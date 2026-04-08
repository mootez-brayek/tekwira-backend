package com.project.tekwira.Services;

import com.project.tekwira.Dto.CreateOWnerRequest;
import com.project.tekwira.Models.User;

public interface AdminService {

    User createOwner(CreateOWnerRequest request);
}
