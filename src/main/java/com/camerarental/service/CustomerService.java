package com.camerarental.service;

import com.camerarental.entity.Customer;
import com.camerarental.entity.RentalOrder;
import com.camerarental.repository.CustomerRepository;
import com.camerarental.repository.RentalOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RentalOrderRepository rentalOrderRepository;

    public CustomerService(CustomerRepository customerRepository,
                           RentalOrderRepository rentalOrderRepository) {
        this.customerRepository = customerRepository;
        this.rentalOrderRepository = rentalOrderRepository;
    }

    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchCustomers(keyword);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + id));
    }

    public List<RentalOrder> getCustomerRentalHistory(Long customerId) {
        return rentalOrderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        String phone = customer.getPhone().trim();
        String cccd = customer.getCccd().trim();

        Optional<Customer> existingPhone = customerRepository.findByPhone(phone);
        if (existingPhone.isPresent() && !existingPhone.get().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Số điện thoại '" + phone + "' đã được đăng ký cho khách hàng '" + existingPhone.get().getFullName() + "'!");
        }

        Optional<Customer> existingCccd = customerRepository.findByCccd(cccd);
        if (existingCccd.isPresent() && !existingCccd.get().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Số CCCD '" + cccd + "' đã được đăng ký cho khách hàng '" + existingCccd.get().getFullName() + "'!");
        }

        customer.setFullName(customer.getFullName().trim());
        customer.setPhone(phone);
        customer.setCccd(cccd);
        return customerRepository.save(customer);
    }
}
