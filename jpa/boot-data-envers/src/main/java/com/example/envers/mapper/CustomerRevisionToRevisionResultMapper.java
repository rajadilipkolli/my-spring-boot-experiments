package com.example.envers.mapper;

import com.example.envers.entities.Customer;
import com.example.envers.model.response.RevisionResult;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Component;

@Component
public class CustomerRevisionToRevisionResultMapper {

    public RevisionResult convert(Revision<Integer, Customer> customerRevision) {
        return new RevisionResult(
                customerRevision.getEntity(),
                customerRevision.getRevisionNumber(),
                customerRevision.getMetadata().getRevisionType().name(),
                customerRevision.getMetadata().getRevisionInstant());
    }
}
