/**
 * Alipay.com Inc.
 * Copyright (c) 2004‐2019 All Rights Reserved.
 */
package edible.simple.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edible.simple.model.Role;
import edible.simple.model.dataEnum.RoleName;

/**
 * @author Kevin Hadinata
 * @version $Id: RoleRepository.java, v 0.1 2019‐09‐11 15:15 Kevin Hadinata Exp $$
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleName roleName);
}