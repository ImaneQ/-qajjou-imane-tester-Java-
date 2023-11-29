package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
	private static final Logger logger = LogManager.getLogger("ParkingServiceTest");
	// on ne mock pas class à tester
	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		logger.debug("Je rentre dans la méthode setUpPerTest()");
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			Ticket ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");
			when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

			when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	// test de l’appel de la méthode ProcessIncomingVehicle() où tout se déroule
	// comme attendu.
	@Test
	public void testProcessIncomingVehicle() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		ParkingSpot parkingSpot = new ParkingSpot(1, any(ParkingType.class), false); 
		int parkingNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		logger.debug(parkingNumber);
		logger.debug(parkingSpot);

		Ticket ticket = new Ticket();

		when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("VWXYZ");
		ticket.setPrice(0);
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setOutTime(null);
		when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
//		ticketDAO.saveTicket(ticket);
		parkingSpotDAO.updateParking(parkingSpot);
		//		logger.debug(isNotAvailable);

		parkingService.processIncomingVehicle();
		verify(parkingSpotDAO).updateParking(parkingSpot);
	}

	// exécution du test dans le cas ou la méthode updateTicket() de ticketDAO
	// renvoie false lors de l'appel de processExitingVehicle
	@Test
	public void processExitingVehicleTestUnableUpdate() {

		// ARRANGE => On indique au mock quoi faire lorsqu'il sera appelé
		Ticket ticket = new Ticket();
		when(ticketDAO.updateTicket(ticket)).thenReturn(false);

		// ACT => On effectue le traitement et on appelle la méthode
		Boolean returnFalse = ticketDAO.updateTicket(ticket);
		parkingService.processExitingVehicle();

		// ASSERT => On vérifie résultat mais aussi que la classe mockée a bien été
		// appelée
		verify(ticketDAO).updateTicket(ticket);
		assertFalse(returnFalse);

	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat
	// l’obtention d’un spot dont l’ID est 1 et qui est disponible.
	@Test
	public void testGetNextParkingNumberIfAvailable() {
		logger.debug("Je rentre dans la méthode testGetNextParkingNumberIfAvailable()");
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		int parkingNumber = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		ParkingSpot parkingSpot = new ParkingSpot(parkingNumber, ParkingType.CAR, true);
		//		ParkingType parkingType = ParkingType.CAR;
		when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);

		parkingService.getNextParkingNumberIfAvailable();
		verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
		verify(parkingSpotDAO).updateParking(parkingSpot);
		assertEquals(parkingNumber, 1);
		assertEquals(parkingSpotDAO.updateParking(parkingSpot), true);
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable()
	// avec pour résultat aucun spot disponible (la méthode renvoie null).
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(null);
		int nextAvailableSpot = parkingSpotDAO.getNextAvailableSlot(null);
		parkingService.getNextParkingNumberIfAvailable();

		verify(parkingSpotDAO).getNextAvailableSlot(null);
		assertNull(nextAvailableSpot);
	}

	/*
	 * test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	 * résultat aucun spot (la méthode renvoie null) car l’argument saisi par
	 * l’utilisateur concernant le type de véhicule est erroné (par exemple,
	 * l’utilisateur a saisi 3 .
	 */
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
		when(inputReaderUtil.readSelection()).thenReturn(3);

		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(null);
		ParkingType parkingType = ParkingType.CAR;
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
		//		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.parkingType, true);

		parkingService.getNextParkingNumberIfAvailable();
		verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
		assertNull(parkingType);
	}

	@Test
	public void processExitingVehicleTest() {
		/*
		 * Complétez le test existant : processExitingVehicleTest ○ Ce test doit
		 * également mocker l’appel à la méthode getNbTicket() implémentée lors de
		 * l’étape précédente.
		 */

		logger.debug("Je rentre dans la méthode processExitingVehicleTest()");
		try {
			Ticket ticket = ticketDAO.getTicket("ABCDEF");
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			Date outTime = new Date();
			ticket.setOutTime(outTime);
			when(parkingSpotDAO.updateParking(ticket.getParkingSpot())).thenReturn(true);
			ParkingSpot parkingSpot = ticket.getParkingSpot();
			ticket.setParkingSpot(parkingSpot);
			// boolean parkingSpotBoolean = parkingSpotDAO.updateParking(parkingSpot);
			when(ticketDAO.getNbTicket(anyString())).thenReturn(4);

			parkingService.processExitingVehicle();

			verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
			// assert().parkingSpot
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// coder des tests unitaires grace aux mocks pour la classe ParkingService
	@Test
	public void processVerifyingVehicleRegNbInDBTest() {
		logger.debug("Je rentre dans la méthode pprocessVerifyingVehicleRegNbInDBTest()");

		parkingService.processExitingVehicle();
	}

}
