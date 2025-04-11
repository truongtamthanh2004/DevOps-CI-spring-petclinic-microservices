package org.springframework.samples.petclinic.customers.web;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {

        Pet pet = setupPet();

        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();

        pet.setName("Basil");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    @Test
    void shouldReturnNotFoundForNonExistingPet() throws Exception {
        given(petRepository.findById(99)).willReturn(Optional.empty());

        mvc.perform(get("/owners/2/pets/99").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCheckPetEquality() {
        Pet pet1 = setupPet();
        Pet pet2 = setupPet();

        // Assert that two pets with the same properties are equal
        assertEquals(pet1, pet2);
        assertEquals(pet1, pet2);

        // Modify one property and assert they are no longer equal
        pet2.setName("DifferentName");
        assertNotEquals(pet1, pet2);
    }

    @Test
    void shouldReturnErrorWhenCreatingPetWithInvalidOwnerId() throws Exception {
        PetRequest petRequest = new PetRequest(1, new Date(), "", 2);

        mvc.perform(post("/owners/-1/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(petRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateNewPetRecordRequest() throws Exception {
        PetRequest petRequest = new PetRequest(1, new Date(), "Max", 2);
        Owner owner = new Owner();
        PetType petType = new PetType();
        petType.setId(2);
        
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(2)).willReturn(Optional.of(petType));
        given(petRepository.save(any(Pet.class))).willReturn(new Pet());

        mvc.perform(post("/owners/1/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(petRequest)))
            .andExpect(status().isCreated());
    }

    @Test 
    void shouldCreateNewPet() throws Exception {
        PetRequest petRequest = new PetRequest(1, new Date(), "Max", 2);
        Owner owner = new Owner();
        Pet pet = new Pet();
        
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.save(any(Pet.class))).willReturn(pet);

        mvc.perform(post("/owners/1/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(petRequest)))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateExistingPet() throws Exception {
        PetRequest petRequest = new PetRequest(2, new Date(), "Updated Name", 2);
        Pet existingPet = new Pet();
        existingPet.setId(2);
        
        given(petRepository.findById(2)).willReturn(Optional.of(existingPet));
        given(petRepository.save(any(Pet.class))).willReturn(existingPet);

        mvc.perform(put("/owners/1/pets/2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(petRequest)))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrowResourceNotFoundForNonExistingOwner() {
        PetRequest petRequest = new PetRequest(1, new Date(), "Max", 2);
        given(ownerRepository.findById(99)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            new PetResource(petRepository, ownerRepository)
                .processCreationForm(petRequest, 99);
        });
    }

    private PetType createPetType(int id, String name) {
        PetType petType = new PetType();
        petType.setId(id);
        petType.setName(name);
        return petType;
    }

    @Test
    void shouldReturnListOfPetTypes() throws Exception {
        List<PetType> petTypes = Arrays.asList(
            createPetType(1, "Cat"),
            createPetType(2, "Dog")
        );
        
        given(petRepository.findPetTypes()).willReturn(petTypes);

        mvc.perform(get("/petTypes")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Cat"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Dog"));
    }
}