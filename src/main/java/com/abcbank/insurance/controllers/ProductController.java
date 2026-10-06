package com.abcbank.insurance.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.insurance.dto.ProductDto;
import com.abcbank.insurance.entities.Product;
import com.abcbank.insurance.entities.Role;
import com.abcbank.insurance.services.ProductService;
import com.abcbank.insurance.util.CurrentUser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/insurance")
public class ProductController {

	@Autowired
	private ProductService pService;
	@Autowired
	private CurrentUser currentUser;

	@PostMapping("/product")
	public Product createProduct(@RequestBody ProductDto productDto) {
		currentUser.require(Role.ADMIN, Role.SUPERADMIN);
		return pService.createProduct(productDto);
	}

	/** Edit an existing product. The UI's "Edit" action on the product details popup. */
	@PutMapping("/product/{id}")
	public Product updateProduct(@PathVariable int id, @RequestBody ProductDto productDto) {
		currentUser.require(Role.ADMIN, Role.SUPERADMIN);
		productDto.setId(id);
		return pService.createProduct(productDto);
	}

	@GetMapping("/product/{id}")
	public Product getProduct(@PathVariable int id) {
		return pService.getProduct(id);
	}

	@GetMapping("/product/list")
	public List<Product> getProducts() {
		return pService.getProducts();
	}

	@DeleteMapping("/product/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProduct(@PathVariable int id) {
		currentUser.require(Role.ADMIN, Role.SUPERADMIN);
		pService.deleteProduct(id);
	}
}
