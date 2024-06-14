package com.example.jndi.mapper;

import com.example.jndi.entities.Driver;
import com.example.jndi.model.request.DriverRequest;
import com.example.jndi.model.response.DriverResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DriverMapper {

    public Driver toEntity(DriverRequest driverRequest) {
        Driver driver = new Driver();
        driver.setText(driverRequest.text());
        return driver;
    }

    public void mapDriverWithRequest(Driver driver, DriverRequest driverRequest) {
        driver.setText(driverRequest.text());
    }

    public DriverResponse toResponse(Driver driver) {
        return new DriverResponse(driver.getId(), driver.getText());
    }

    public List<DriverResponse> toResponseList(List<Driver> driverList) {
        return driverList.stream().map(this::toResponse).toList();
    }
}
