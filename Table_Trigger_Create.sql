VARIABLE Connection_identification NUMBER;
VARIABLE Booking_identification NUMBER;
BEGIN
:Connection_identification:=0;
END;
.
/
BEGIN
:Booking_identification:=0;
END;
.
/


CREATE TABLE Flight (
    Flight_Number CHAR(8),
    Departure_City CHAR(32),
    Destination_City CHAR(32),
    Departure_Time DATE,
    Arrival_Time DATE,
    Fare REAL,
    Seats_Limit INTEGER,
    PRIMARY KEY (Flight_Number)
);

CREATE TABLE Customer (
    ID CHAR(16),
    Name CHAR(32),
    Nationality CHAR(32),
    Passport_Number CHAR(16),
    PRIMARY KEY (ID),
    UNIQUE (Passport_Number)
);

CREATE TABLE Connection (
    First_Flight CHAR(8) NOT NULL,
    Second_Flight CHAR(8),
    Third_Flight CHAR(8),
    ID INTEGER,
    FOREIGN KEY (First_Flight) REFERENCES Flight(Flight_Number),
    FOREIGN KEY (Second_Flight) REFERENCES Flight(Flight_Number),
    FOREIGN KEY (Third_Flight) REFERENCES Flight(Flight_Number),
    PRIMARY KEY (ID)
);

CREATE TABLE Booking (
    Connection_ID INTEGER,
    Customer_ID CHAR(16),
    Fare REAL,
    ID CHAR(16),
    FOREIGN KEY (Connection_ID) REFERENCES Connection(ID),
    FOREIGN KEY (Customer_ID) REFERENCES Customer(ID),
    PRIMARY KEY (ID)
);

CREATE OR REPLACE TRIGGER Add_booking
AFTER INSERT ON Booking
FOR EACH ROW
BEGIN
    UPDATE Flight SET Seats_Limit=Seats_Limit-1 
    WHERE Flight.Flight_Number IN 
        (SELECT Connection.First_Flight FROM Connection WHERE Connection.ID=:new.ID);
    UPDATE Flight SET Seats_Limit=Seats_Limit-1 
    WHERE Flight.Flight_Number IN 
        (SELECT Connection.Second_Flight FROM Connection WHERE Connection.ID=:new.ID);
    UPDATE Flight SET Seats_Limit=Seats_Limit-1 
    WHERE Flight.Flight_Number IN 
        (SELECT Connection.Third_Flight FROM Connection WHERE Connection.ID=:new.ID);
END;
.
/

CREATE OR REPLACE TRIGGER Delete_booking
AFTER DELETE ON Booking
FOR EACH ROW
DECLARE 
c1 INTEGER; c2 INTEGER;  c3 INTEGER; c4 INTEGER;
BEGIN
    SELECT COUNT(*) into c1 FROM Flight WHERE Flight.Flight_Number IN
    (SELECT First_Flight FROM Connection WHERE Connection.ID=:old.Connection_ID);
    SELECT COUNT(*) into c2 FROM Flight WHERE Flight.Flight_Number IN
    (SELECT Second_Flight FROM Connection WHERE Connection.ID=:old.Connection_ID);
    SELECT COUNT(*) into c1 FROM Flight WHERE Flight.Flight_Number IN
    (SELECT Third_Flight FROM Connection WHERE Connection.ID=:old.Connection_ID);
    IF(c1=1) THEN
        UPDATE Flight SET Seats_Limit=Seats_Limit+1 
        WHERE Flight.Flight_Number IN 
        (SELECT Connection.First_Flight FROM Connection WHERE Connection.ID=:old.Connection_ID);
    END IF;
    IF(c2=1) THEN
        UPDATE Flight SET Seats_Limit=Seats_Limit+1 
        WHERE Flight.Flight_Number IN 
        (SELECT Connection.Second_Flight FROM Connection WHERE Connection.ID=:old.Connection_ID);
    END IF;
    IF(c3=1) THEN
        UPDATE Flight SET Seats_Limit=Seats_Limit+1 
        WHERE Flight.Flight_Number IN 
        (SELECT Connection.Third_Flight FROM Connection WHERE Connection.ID=:old.Connection_ID);
    END IF;
    SELECT COUNT(*) into c4 FROM Booking WHERE Booking.Connection_ID =:old.Connection_ID;
    IF(c4=0) THEN
        DELETE FROM Connection WHERE Connection.ID=:old.Connection_ID;
    END IF;
END;
.
/






CREATE TYPE my_str AS TABLE OF CHAR;
/
CREATE OR REPLACE PROCEDURE book_flight(cus_id my_str , First_Flight my_str , Second_Flight my_str , Third_Flight my_str)
AS valid_para INTEGER; c INTEGER; total_fare REAL; f1 REAL; f2 REAL; f3 REAL; i INTEGER; strFliNum CHAR(8); strCusNum CHAR(16);
BEGIN
    valid_para:=1;
    SELECT COUNT(*) into c FROM Flight WHERE Flight.Flight_Number=First_Flight;
    IF(c=0) THEN
        valid_para:=0;
    END IF;
    IF(Second_Flight<>NULL) THEN
        SELECT COUNT(*) into c FROM Flight WHERE Flight.Flight_Number=Second_Flight;
        IF(c=0) THEN
            valid_para:=0;
        END IF;
    END IF;
    IF(Third_Flight<>NULL) THEN
        SELECT COUNT(*) into c FROM Flight WHERE Flight.Flight_Number=Third_Flight;
        IF(c=0) THEN
            valid_para:=0;
        END IF;
    END IF;
    IF(valid_para=1) THEN
        INSERT INTO Connection VALUES(First_Flight, Second_Flight, Third_Flight, :Connection_identification);
        IF(Third_Flight =NULL) THEN
            IF(Second_Flight =NULL) THEN
SELECT Fare INTO total_fare FROM Flight WHERE Flight.Flight_Number=First_Flight;
            ELSE
SELECT Fare INTO f1 FROM Flight WHERE Flight.Flight_Number=First_Flight;
SELECT Fare INTO f2 FROM Flight WHERE Flight.Flight_Number=Second_Flight;
total_fare:=(f1+f2)*0.9;
END IF;
ELSE
SELECT Fare INTO f1 FROM Flight WHERE Flight.Flight_Number=First_Flight;
SELECT Fare INTO f2 FROM Flight WHERE Flight.Flight_Number=Second_Flight;
SELECT Fare INTO f3 FROM Flight WHERE Flight.Flight_Number=Third_Flight;
total_fare:=(f1+f2+f3)*0.75;
END IF;
INSERT INTO Booking VALUES(:Connection_identification, cus_id, total_fare, :Booking_identification);
    :Booking_identification:=:Booking_identification+1;
:Connection_identification:=:Connection_identification+1;
    END IF;
--DBMS.OUTPUT.PUT_LINE(valid_para);
END;
.
/
    
Create or replace trigger Flight_booking_delete
AFTER DELETE ON Flight
FOR EACH ROW
BEGIN
 
DELETE FROM Booking
WHERE Connection_ID IN (SELECT ID FROM Connection
WHERE :old.Flight_Number= First_Flight
OR :old.Flight_Number= Second_Flight
OR :old.Flight_Number=Third_Flight);
 
END;
.
/


CREATE OR REPLACE PROCEDURE book_flight(cus_id my_str , First_Flight my_str , Second_Flight my_str , Third_Flight my_str)
AS
valid_para INTEGER; c INTEGER; total_fare REAL; f1 REAL; f2 REAL; f3 REAL; i
INTEGER; strFliNum CHAR(8); strCusNum CHAR(16);
BEGIN
            valid_para:=1;
            SELECT COUNT(*) into c FROM Flight WHERE Flight.Flight_Number=First_Flight;
            IF(c=0) THEN
                        valid_para:=0;
            END IF;
            IF(Second_Flight<>NULL) THEN
                        SELECT COUNT(*) into c
FROM Flight WHERE Flight.Flight_Number=Second_Flight;
                        IF(c=0) THEN
                                    valid_para:=0;
                        END IF;
            END IF;
            IF(Third_Flight<>NULL) THEN
                        SELECT COUNT(*) into c
FROM Flight WHERE Flight.Flight_Number=Third_Flight;
                        IF(c=0) THEN
                                    valid_para:=0;
                        END IF;
            END IF;
            IF(valid_para=1) THEN
                        INSERT INTO Connection
VALUES(First_Flight, Second_Flight, Third_Flight, Connection_identification);
                        IF(Third_Flight =NULL)
THEN
                                    IF(Second_Flight =NULL) THEN
SELECT
Fare INTO total_fare FROM Flight WHERE Flight.Flight_Number=First_Flight;
                                    ELSE
SELECT
Fare INTO f1 FROM Flight WHERE Flight.Flight_Number=First_Flight;
SELECT
Fare INTO f2 FROM Flight WHERE Flight.Flight_Number=Second_Flight;
total_fare:=(f1+f2)*0.9;
END IF;
ELSE
SELECT
Fare INTO f1 FROM Flight WHERE Flight.Flight_Number=First_Flight;
SELECT
Fare INTO f2 FROM Flight WHERE Flight.Flight_Number=Second_Flight;
SELECT
Fare INTO f3 FROM Flight WHERE Flight.Flight_Number=Third_Flight;
total_fare:=(f1+f2+f3)*0.75;
END IF;
INSERT
INTO Booking VALUES(Connection_identification, cus_id, total_fare, Booking_identification);
            Booking_identification:=Booking_identification+1;
Connection_identification:=Connection_identification+1;
            END IF;
--DBMS.OUTPUT.PUT_LINE(valid_para);
END;
.
/
 

