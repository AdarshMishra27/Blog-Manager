package com.example.BlogManager.objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Table(name = "users") //because user is a reserved keyword in postgres
@Entity
@Getter
@Setter
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String userId; //username OR userId is used interchangeably in the app

    private String password; // hashed

    private UserType userType;


    //one user can have many blogs
    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-blogs")  // This manages serialization of the child list
    private List<Blog> blogs;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "user-comments")  // This manages serialization of the child list
    private List<Comment> comments;


    public UserEntity() {
    }

    public UserEntity(String name, String userId, String password, UserType userType) {
        this.name = name;
        this.userId = userId;
        this.password = password;
        this.userType = userType;
    }
}
