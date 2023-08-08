package com.example.envers.mapper;

import com.example.envers.entities.Customer;
import com.example.envers.model.RevisionDTO;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Component;

@Component
public class CustomerRevisionToRevisionDTOMapper {

    public RevisionDTO convert(Revision<Integer, Customer> customerRevision) {
        return new RevisionDTO(
                customerRevision.getEntity(),
                customerRevision.getMetadata().getRevisionNumber(),
                customerRevision.getMetadata().getRevisionType().name(),
                customerRevision.getMetadata().getRevisionInstant());
    }
}
