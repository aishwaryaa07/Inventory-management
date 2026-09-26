package com.aditya.stockmanager.web;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aditya.stockmanager.domain.Product;
import com.aditya.stockmanager.exception.ProductNotDeletableException;
import com.aditya.stockmanager.exception.ProductNotFoundException;
import com.aditya.stockmanager.service.ProductService;
import com.aditya.stockmanager.service.StockService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

	@MockBean
	private ProductService productService;

	@MockBean
	private StockService stockService;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void getAll_returnsEveryProduct() throws Exception {
		Product laptop = Product.builder().id(1L).name("Laptop").quantity(10).reorderLevel(5).build();
		when(productService.findAll()).thenReturn(List.of(laptop));

		mockMvc.perform(get("/api/products")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Laptop"));
	}

	@Test
	void getOne_returns404WhenMissing() throws Exception {
		when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

		mockMvc.perform(get("/api/products/{id}", 99L)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void create_returns201() throws Exception {
		Product created = Product.builder().id(1L).name("Laptop").quantity(10).reorderLevel(5).build();
		when(productService.create(org.mockito.ArgumentMatchers.any())).thenReturn(created);

		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"Laptop\", \"quantity\": 10}")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void create_returns400WhenNameBlank() throws Exception {
		mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"\", \"quantity\": 10}")).andExpect(status().isBadRequest());
	}

	@Test
	void delete_returns204() throws Exception {
		doNothing().when(productService).delete(1L);

		mockMvc.perform(delete("/api/products/{id}", 1L)).andExpect(status().isNoContent());
	}

	@Test
	void delete_returns409WhenStillHasStock() throws Exception {
		org.mockito.Mockito.doThrow(new ProductNotDeletableException("Laptop", 3)).when(productService).delete(1L);

		mockMvc.perform(delete("/api/products/{id}", 1L)).andExpect(status().isConflict());
	}

	@Test
	void stockIn_returnsUpdatedProduct() throws Exception {
		Product updated = Product.builder().id(1L).name("Laptop").quantity(15).reorderLevel(5).build();
		when(stockService.stockIn(1L, 5)).thenReturn(updated);

		mockMvc.perform(post("/api/products/{id}/stock-in", 1L).contentType(MediaType.APPLICATION_JSON)
				.content("{\"quantity\": 5}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(15));
	}

}
