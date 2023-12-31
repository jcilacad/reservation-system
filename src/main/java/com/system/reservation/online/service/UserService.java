package com.system.reservation.online.service;

import com.system.reservation.online.dto.ChangePasswordDto;
import com.system.reservation.online.dto.UserDto;
import com.system.reservation.online.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.awt.print.Pageable;
import java.security.Principal;
import java.util.List;

public interface UserService {

    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllDto();

    boolean isUserExists(UserDto user);

    boolean isUserAuthenticated(Authentication authentication);

    List<User> findAll();

    User findByStudentId(Long studentId);

    UserDto mapUserToDto(User user);

    Page<User> findAllPaginated(Integer currentPage, Integer pageSize);

    void updateStudentDetailsById(Long studentId, UserDto userDto);

    void deleteStudentById(Long studentId);

    Page<User> findStudentByNameContaining(String name, Integer currentPage, Integer pageSize);

    boolean changePassword(Principal principal, ChangePasswordDto changePasswordDto);

}
