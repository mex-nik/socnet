package mx.demo.socnet.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 04.06.2021
 * @project socnet
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
@ToString
public class UserData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String admin;
    private String gender;
    private String country;
    private String password;
    @OneToMany(mappedBy = "user")
    private List<UserPost> posts;
}
