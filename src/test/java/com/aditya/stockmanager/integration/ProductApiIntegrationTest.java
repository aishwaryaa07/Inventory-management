package com.aditya.stockmanager.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.aditya.stockmanager.dto.ApiError;
import com.aditya.stockmanager.dto.CreateProductRequest;
import com.aditya.stockmanager.dto.ProductResponse;
import com.aditya.stockmanager.dto.StockAdjustmentRequest;
import com.aditya.stockmanager.dto.StockMovementResponse;

/**
 * End-to-end tests that exercise the real HTTP layer against a real, disposable MySQL
 * instance (via Testcontainers) instead of mocks - complements the Mockito-based service
 * tests and the MockMvc-based controller tests with a full-stack check.
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProductApiIntegrationTest {

	@Container
	@ServiceConnection
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	@org.springframework.beans.factory.annotation.Autowired
	private TestRestTemplate restTemplate;

	@Test
	void createProduct_thenItIsRetrievable() {
		ProductResponse created = createProduct("Laptop", 10, null);

		ResponseEntity<ProductResponse> fetched = restTemplate.getForEntity("/api/products/" + created.id(),
				ProductResponse.class);

		assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(fetched.getBody().name()).isEqualTo("Laptop");
		assertThat(fetched.getBody().quantity()).isEqualTo(10);
		assertThat(fetched.getBody().reorderLevel()).isEqualTo(5);
	}

	@Test
	void createProduct_duplicateName_returns409() {
		createProduct("Duplicate Me", 1, null);

		ResponseEntity<ApiError> response = restTemplate.postForEntity("/api/products",
				new CreateProductRequest("Duplicate Me", 1, null), ApiError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().message()).contains("Duplicate Me");
	}

	@Test
	void createProduct_blankName_returns400() {
		ResponseEntity<ApiError> response = restTemplate.postForEntity("/api/products",
				new CreateProductRequest("", 1, null), ApiError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void getProduct_missing_returns404() {
		ResponseEntity<ApiError> response = restTemplate.getForEntity("/api/products/999999", ApiError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void stockInThenStockOut_updatesQuantityAndRecordsMovements() {
		ProductResponse created = createProduct("Keyboard", 20, null);

		ResponseEntity<ProductResponse> afterStockIn = restTemplate.postForEntity(
				"/api/products/" + created.id() + "/stock-in", new StockAdjustmentRequest(5), ProductResponse.class);
		assertThat(afterStockIn.getBody().quantity()).isEqualTo(25);

		ResponseEntity<ProductResponse> afterStockOut = restTemplate.postForEntity(
				"/api/products/" + created.id() + "/stock-out", new StockAdjustmentRequest(8), ProductResponse.class);
		assertThat(afterStockOut.getBody().quantity()).isEqualTo(17);

		ResponseEntity<StockMovementResponse[]> movements = restTemplate.getForEntity(
				"/api/products/" + created.id() + "/transactions", StockMovementResponse[].class);
		assertThat(movements.getBody()).hasSize(2);
	}

	@Test
	void stockOut_moreThanAvailable_returns400() {
		ProductResponse created = createProduct("Monitor", 3, null);

		ResponseEntity<ApiError> response = restTemplate.postForEntity("/api/products/" + created.id() + "/stock-out",
				new StockAdjustmentRequest(99), ApiError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().message()).contains("Monitor");
	}

	@Test
	void deleteProduct_blockedWhileStockRemains_returns409() {
		ProductResponse created = createProduct("Chair", 4, null);

		ResponseEntity<ApiError> response = restTemplate.exchange("/api/products/" + created.id(),
				org.springframework.http.HttpMethod.DELETE, HttpEntity.EMPTY, ApiError.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	private ProductResponse createProduct(String name, int quantity, Integer reorderLevel) {
		ResponseEntity<ProductResponse> response = restTemplate.postForEntity("/api/products",
				new CreateProductRequest(name, quantity, reorderLevel), ProductResponse.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		return response.getBody();
	}

}
