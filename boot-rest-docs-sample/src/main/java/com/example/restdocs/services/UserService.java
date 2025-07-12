package com.example.restdocs.services;

import com.example.restdocs.entities.User;
import com.example.restdocs.model.request.UserRequest;
import com.example.restdocs.model.response.PagedResult;
import com.example.restdocs.repositories.UserRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedResult<User> findAllUsers(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<User> usersPage = userRepository.findAll(pageable);

        return new PagedResult<>(usersPage);
    }

    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(UserRequest userRequest) {

        User user = mapUserRequestToUser(userRequest);
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(User userObj, UserRequest userRequest) {
        updateUserFromUserRequest(userRequest, userObj);
        return userRepository.save(userObj);
    }

    private User mapUserRequestToUser(UserRequest userRequest) {
        return new User()
                .setFirstName(userRequest.firstName())
                .setLastName(userRequest.lastName())
                .setAge(userRequest.age())
                .setGender(userRequest.gender())
                .setPhoneNumber(userRequest.phoneNumber());
    }

    void updateUserFromUserRequest(UserRequest userRequest, User user) {
        user.setFirstName(userRequest.firstName());
        user.setLastName(userRequest.lastName());
        user.setAge(userRequest.age());
        user.setGender(userRequest.gender());
        user.setPhoneNumber(userRequest.phoneNumber());
    }
}
