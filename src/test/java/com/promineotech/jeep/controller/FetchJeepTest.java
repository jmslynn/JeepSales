 package com.promineotech.jeep.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import com.promineotech.jeep.Constants;
import com.promineotech.jeep.controller.support.FetchJeepTestSupport;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;



//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes= {JeepSales.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {
	    "classpath:flyway/migrations/V1.0__Jeep_Schema.sql",
	    "classpath:flyway/migrations/V1.1__Jeep_Data.sql"}, 
	    config = @SqlConfig(encoding = "utf-8"))

class FetchJeepTest extends FetchJeepTestSupport {
	
	@Test
	void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
 	 //given: a valid model, trim and uri
		JeepModel model = JeepModel.WRANGLER;
		String trim = "Sport";
		String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
		
		
		//when: a connection is made to the uri
		ResponseEntity<List<Jeep>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, 
				new ParameterizedTypeReference<>() {});
	
		
		//then: a success (OK - 200) status code is returned
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		
		//and: the actual list returned is the same as the expected list
		List<Jeep> actual = response.getBody();
		List<Jeep> expected = buildExpected();
		
		actual.forEach(jeep -> jeep.setModelPK(null));
		
		assertThat(actual).isEqualTo(expected);
		 
	}//end test
	
	@Test
	void testThatAnErrorMessageIsReturnedWhenAnUnknownTrimIsSupplied() {
 	 //given: a valid model, trim and uri
		JeepModel model = JeepModel.WRANGLER;
		String trim = "Unknown Value";
		String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
		
		
		//when: a connection is made to the uri
		ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, 
				new ParameterizedTypeReference<>() {});
	
		
		//then: a not found (404) status code is returned
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		
		//and: an error messages is returned
		Map<String, Object> error = response.getBody();               
		
		assertErrorMessageValid(error, HttpStatus.NOT_FOUND);

		
	}//end test

	@ParameterizedTest
	@MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidTest")
	void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(String model, String trim, String reason) {
 	 //given: a valid model, trim and uri

		String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
		
		
		//when: a connection is made to the uri
		ResponseEntity<Map<String, Object>> response = getRestTemplate().exchange(uri, HttpMethod.GET, null, 
				new ParameterizedTypeReference<>() {});
	
		
		//then: a bad request (400) status code is returned
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		
		//and: an error messages is returned
		Map<String, Object> error = response.getBody(); 
		
		assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
		
	}//end test

static Stream<Arguments> parametersForInvalidTest() {
	//@formatter:off
	return Stream.of(
			arguments("WRANGLER", "@##%^$$#$", "Trim contains non-alpha-numeric characters"),
			arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH+1), "Trim length too long"),
			arguments("INVALID", "Sport", "Model is not enum value")
			);
	//@formatter:on
}
}//end class
   