package pl.s30331.ScoutForce.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Club director who creates delegations and assigns scouts to them.
 */
@Entity
@Table(name = "director")
@PrimaryKeyJoinColumn(name = "person_id")
@Getter
@Setter
@NoArgsConstructor
public class Director extends ClubEmployee {

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Delegation> createdDelegations = new ArrayList<>();

    @OneToMany(mappedBy = "sentByDirector", fetch = FetchType.LAZY)
    private List<Scout> sentScouts = new ArrayList<>();
}
