package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuestService {
    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }


    public Guest addGuest(CreateGuestRequestDTO requestDTO) {
        Guest guest = new Guest();
        guest.setName(requestDTO.getName());
        Guest savedGuest = guestRepository.save(guest);
        return mapToGuestDTO(savedGuest);
    }
    public Guest updateGuest(Long guestId, UpdateGuestRequestDTO requestDTO) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new EntityNotFoundException("Guest not found with guestId: " + guestId));

        guest.setName(requestDTO.getName());
        Guest updatedGuest = guestRepository.save(guest);
        return mapToGuestDTO(updatedGuest);
    }

    public void deleteGuest(Long guestId) {
        guestRepository.deleteById(guestId);
    }

    public Guest findGuestById(Long guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new NotFoundException("Guest not found with guestId: " + guestId));
        return mapToGuestDTO(guest);
    }

    public List<Guest> listAllGuests() {
        List<Guest> guests = guestRepository.findAll();
        return guests.stream().map(this::mapToGuestDTO).collect(Collectors.toList());
    }

    private Guest mapToGuestDTO(Guest guest) {
        Guest guestDTO = new Guest();
        guestDTO.setId(guest.getId());
        guestDTO.setName(guest.getName());
        return guestDTO;
    }
}
