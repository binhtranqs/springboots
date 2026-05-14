package com.example.day1.service;

import com.example.day1.dto.CreateUserRequest;
import com.example.day1.dto.UpdateUserRequest;
import com.example.day1.model.User;
import com.example.day1.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public List<User> getUsers() {

        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return findUserOrThrow(id);
    }

    public User createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return userRepository.save(user);
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        userRepository.findByEmail(request.getEmail())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });

        User updatedUser = new User();
        updatedUser.setName(request.getName());
        updatedUser.setEmail(request.getEmail());
        return userRepository.update(id, updatedUser);
    }

    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
