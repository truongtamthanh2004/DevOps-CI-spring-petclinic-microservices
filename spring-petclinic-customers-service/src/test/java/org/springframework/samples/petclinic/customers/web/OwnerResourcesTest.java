package org.springframework.samples.petclinic.customers.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {
  @Autowired
  MockMvc mvc;

  @MockBean
  OwnerRepository ownerRepository;

  @MockBean
  OwnerEntityMapper ownerEntityMapper;

  @Test
  void testToString() {
    Owner owner = new Owner();
    owner.setId(1);
    owner.setFirstName("John");
    owner.setLastName("Doe");
    owner.setAddress("123 Main St");
    owner.setCity("Springfield");
    owner.setTelephone("1234567890");

    String toString = owner.toString();

    assertThat(toString)
      .contains("id = 1")
      .contains("lastName = 'Doe'")
      .contains("firstName = 'John'")
      .contains("address = '123 Main St'")
      .contains("city = 'Springfield'")
      .contains("telephone = '1234567890'");
  }

  @Test
  void shouldGetOwnerById() throws Exception {
    Owner owner = new Owner();
    owner.setId(1);
    owner.setFirstName("John");
    owner.setLastName("Doe");

    given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

    mvc.perform(get("/owners/1")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.firstName").value("John"))
      .andExpect(jsonPath("$.lastName").value("Doe"));
  }

  @Test
  void shouldReturnNotFoundForNonExistingOwner() throws Exception {
    given(ownerRepository.findById(1)).willReturn(Optional.empty());

    mvc.perform(get("/owners/1")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(content().string("null"));
  }

  @Test
  void shouldGetAllOwners() throws Exception {
    Owner owner1 = new Owner();
    owner1.setId(1);
    owner1.setFirstName("John");
    
    Owner owner2 = new Owner();  
    owner2.setId(2);
    owner2.setFirstName("Jane");

    List<Owner> owners = Arrays.asList(owner1, owner2);

    given(ownerRepository.findAll()).willReturn(owners);

    mvc.perform(get("/owners")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].firstName").value("John"))
      .andExpect(jsonPath("$[1].id").value(2))
      .andExpect(jsonPath("$[1].firstName").value("Jane"));
  }
}