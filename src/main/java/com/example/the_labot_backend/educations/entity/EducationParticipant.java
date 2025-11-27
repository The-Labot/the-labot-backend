package com.example.the_labot_backend.educations.entity;

import com.example.the_labot_backend.workers.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "safety_education_participant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 교육인지
    @ManyToOne
    @JoinColumn(name = "education_id")
    private Education education;

    // 어떤 근로자인지
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;
}
