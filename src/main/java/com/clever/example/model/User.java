package com.clever.example.model;

import com.clever.example.model.abstracts.BaseEntity;
import lombok.Data;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.Objects;

@Data
@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at=now() WHERE id=?")
public class User extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cellphone")
    private String cellphone;

    @Temporal(TemporalType.DATE)
    @Column(name = "birthdate")
    private Date birthdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
