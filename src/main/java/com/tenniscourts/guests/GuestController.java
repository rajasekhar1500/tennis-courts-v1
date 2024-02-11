package com.tenniscourts.guests;

import com.tenniscourts.schedules.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@Api(tags = "Guest Management")
@RequestMapping("/api/guests")
@AllArgsConstructor
@Slf4j
public class GuestController {

    private final GuestService guestService;

    private final ScheduleService scheduleService;

    @PostMapping("/add")
    @ApiOperation("Add Guest")
    public ResponseEntity<Guest> createGuest(@Valid @RequestBody CreateGuestRequestDTO requestDTO) {
        Guest createdGuest = guestService.addGuest(requestDTO);
        return ResponseEntity.created(URI.create("/admin/guest/" + createdGuest.getId())).body(createdGuest);
    }

    @PutMapping("/update/{guestId}")
    @ApiOperation("Update Guest")
    public ResponseEntity<Guest> updateGuest(@PathVariable Long guestId, @Valid @RequestBody UpdateGuestRequestDTO requestDTO) {
        Guest updatedGuest = guestService.updateGuest(guestId, requestDTO);
        return ResponseEntity.ok(updatedGuest);
    }

    @DeleteMapping("/delete/{guestId}")
    @ApiOperation("Delete Guest")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/findbyid/{guestId}")
    @ApiOperation("Find Guest By Id")
    public ResponseEntity<Guest> findGuestById(@PathVariable Long guestId) {
        Guest guest = guestService.findGuestById(guestId);
        return ResponseEntity.ok(guest);
    }

    @GetMapping("/findall")
    @ApiOperation("Get all guests")
    public ResponseEntity<List<Guest>> listAllGuests() {
        List<Guest> guests = guestService.listAllGuests();
        return ResponseEntity.ok(guests);
    }

}
