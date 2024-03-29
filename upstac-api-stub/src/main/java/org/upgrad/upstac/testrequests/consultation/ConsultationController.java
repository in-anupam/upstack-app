package org.upgrad.upstac.testrequests.consultation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;

    // It helps fetching all the Test Requests with Status as Completed, in the Doctor bucket
    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {

        return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);

    }

    // It helps fetching all the assigned Test Requests for the logged-in Doctor
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {

        User doctor = userLoggedInService.getLoggedInUser();
        return testRequestQueryService.findByDoctor(doctor);

    }


    // It helps fetching all the assigned Test Requests for the logged-in Doctor
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {

        try {

            User doctor = userLoggedInService.getLoggedInUser();
            TestRequest testRequest = testRequestUpdateService.assignForConsultation(id, doctor);
            return testRequest;

        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

    // It helps updating the assigned Test Requests of a Doctor
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {

        try {

            User doctor = userLoggedInService.getLoggedInUser();
            TestRequest testRequest = testRequestUpdateService.updateConsultation(id, testResult,doctor);
            return testRequest;

        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

}
