package com.docbranch.repository.trash;

import com.docbranch.domain.trash.TrashItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrashItemRepository extends JpaRepository<TrashItem, UUID> {
}
