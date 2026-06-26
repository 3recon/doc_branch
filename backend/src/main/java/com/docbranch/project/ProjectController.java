package com.docbranch.project;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectDetailResponse> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectDetailResponse response = projectService.createProject(request);
        return ResponseEntity
                .created(URI.create("/api/projects/" + response.projectId()))
                .body(response);
    }

    @PatchMapping("/{projectId}")
    public ProjectDetailResponse updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectDeleteRequest request
    ) {
        projectService.deleteProject(projectId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public List<ProjectMemberResponse> getProjectMembers(@PathVariable UUID projectId) {
        return projectService.getProjectMembers(projectId);
    }

    @PatchMapping("/{projectId}/members/{projectMemberId}")
    public ProjectMemberResponse updateProjectMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID projectMemberId,
            @Valid @RequestBody ProjectMemberRoleUpdateRequest request
    ) {
        return projectService.updateProjectMemberRole(projectId, projectMemberId, request);
    }

    @DeleteMapping("/{projectId}/members/{projectMemberId}")
    public ResponseEntity<Void> removeProjectMember(
            @PathVariable UUID projectId,
            @PathVariable UUID projectMemberId
    ) {
        projectService.removeProjectMember(projectId, projectMemberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<ProjectSummaryResponse> getProjects() {
        return projectService.getProjects();
    }

    @GetMapping("/{projectId}")
    public ProjectDetailResponse getProject(@PathVariable UUID projectId) {
        return projectService.getProject(projectId);
    }
}
