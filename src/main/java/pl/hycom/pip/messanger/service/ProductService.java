package pl.hycom.pip.messanger.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pl.hycom.pip.messanger.model.Keyword;
import pl.hycom.pip.messanger.model.Product;
import pl.hycom.pip.messanger.repository.ProductRepository;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Log4j2
public class ProductService {
    private final ProductRepository productRepository;

    public Product addProduct(Product product) {
        log.info("Invoking of addProduct(product) method from ProductService class");
        return productRepository.save(product);
    }

    public Product findProductById(Integer id) {
        log.info("Invoking of findProductById(id) method from ProductService class");
        return productRepository.findOne(id);
    }

    public List<Product> findAllProducts() {
        log.info("Invoking of findAllProducts() method from ProductService class");
        return StreamSupport.stream(productRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void deleteProduct(Integer id) {
        log.info("Invoking of deleteProduct(id) method from ProductService class");
        productRepository.delete(id);
    }

    public Product updateProductName(Integer id, String newName) {
        log.info("Invoking of updateProductName(id, newName) method from ProductService class");
        Product product = productRepository.findOne(id);
        product.setName(newName);
        return productRepository.save(product);
    }

    public Product updateProduct(Product product) {
        log.info("Invoking of updateProductName(id, newName) method from ProductService class");
        Product updatedProduct = productRepository.findOne(product.getId());
        updatedProduct.setName(product.getName());
        updatedProduct.setDescription(product.getDescription());
        updatedProduct.setImageUrl(product.getImageUrl());
        return productRepository.save(updatedProduct);
    }

    public Product addOrUpdateProduct(Product product) {
        if (product.getId() != 0) {
            Product updatedProduct = updateProduct(product);
            log.info("Product updated !!!");
            return updatedProduct;
        }

        Product addedProduct = addProduct(product);
        log.info("Product added !!!");
        return addedProduct;
    }

    public List<Product> getRandomProducts(int howManyProducts) {
        List<Product> products = new ArrayList<>(howManyProducts);
        int quantity = (int) productRepository.count();
        if (quantity == 0 || howManyProducts > quantity) {
            products.addAll(findAllProducts());
            return products;
        }
        for (int i = 0; i < howManyProducts; i++) {
            PageRequest pr = new PageRequest(new Random().nextInt(quantity - products.size()), 1);
            products.addAll(productRepository.findSomeProducts(products, pr));
        }

        return products;
    }

    public List<Product> findAllProductsContainingAtLeastOneKeyword(Keyword... keywords) {
        return Arrays.stream(Optional.ofNullable(keywords).orElse(new Keyword[] {})).filter(Objects::nonNull).flatMap
                (k -> productRepository.findProductsWithKeyword(k).stream()).filter(Objects::nonNull).distinct().
                collect(Collectors.toList());
    }

    public Product addKeywordsToProduct(Integer id, Keyword... keywords) {
        Product product = findProductById(id);
        product.getKeywords().addAll(Arrays.asList(keywords));
        return productRepository.save(product);
    }

    public Product removeKeywordsFromProduct(Integer id, Keyword... keywords) {
        Product product = findProductById(id);
        product.getKeywords().removeAll(Arrays.asList(keywords));
        return productRepository.save(product);
    }

    public void deleteAllProducts() {
        productRepository.deleteAll();
    }

    public int count() {
        return findAllProducts().size();
    }
}
