package com.github.hallbm.chesswithcats.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class AuthorityRepositoryTest {
/*
	@Autowired
	AuthorityRepository authRepo;

	Authority auth1, auth2, auth3;

	@BeforeEach
	public void setUp() {
		auth1 = new Authority(1L, "ROLE_ADMIN");		
		auth2 = new Authority(1L, "ROLE_USER");
		auth3 = new Authority(1L, "ROLE_CUSTOMER");
	}

    @DisplayName("Test for FindByAuthority operation, ex1")
	@Test
    public void givenAuthority_whenFindByAuthority_thenReturnAuthorityObject1() {
    	authRepo.save(auth1);
		Authority authDB = authRepo.findByAuthority(auth1.getAuthority()).get();
    	assertThat(authDB).isNotNull();
    	assertEquals(authDB.getAuthority(), auth1.getAuthority());
    }
	
    @DisplayName("Test for FindByAuthority operation, ex2")
	@Test
    public void givenAuthority_whenFindByAuthority_thenReturnAuthorityObject2() {
    	authRepo.save(auth2);
		Authority authDB = authRepo.findByAuthority(auth2.getAuthority()).get();
    	assertThat(authDB).isNotNull();
    	assertEquals(authDB.getAuthority(), auth2.getAuthority());
    }
    
    @DisplayName("Test for FindByAuthority operation when Auth not present, ex3")
	@Test
    public void givenNotSavedAuthority_whenFindByAuthority_thenReturnNull() {
		Authority authDB = authRepo.findByAuthority(auth3.getAuthority()).orElse(null);
    	assertThat(authDB).isNull();
    }
  */  
}
