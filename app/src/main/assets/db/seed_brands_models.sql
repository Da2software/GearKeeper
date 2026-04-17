PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

-- Brands: cars
INSERT OR IGNORE INTO brand (id, name, vehicle_type, is_seeded) VALUES
    (1, 'Toyota', 'car', 1),
    (2, 'Honda', 'car', 1),
    (3, 'Ford', 'car', 1),
    (4, 'Hyundai', 'car', 1),
    (5, 'Kia', 'car', 1),
    (6, 'Volkswagen', 'car', 1),
    (7, 'Nissan', 'car', 1),
    (8, 'Chevrolet', 'car', 1),
    (9, 'Tesla', 'car', 1),
    (10, 'Mazda', 'car', 1),
    (11, 'BMW', 'car', 1),
    (12, 'Mercedes-Benz', 'car', 1),
    (13, 'Audi', 'car', 1),
    (14, 'Renault', 'car', 1),
    (15, 'Peugeot', 'car', 1),
    (16, 'MG', 'car', 1),
    (17, 'Chirey', 'car', 1),
    (18, 'SEAT', 'car', 1),
    (19, 'Cupra', 'car', 1),
    (20, 'JAC', 'car', 1),
    (21, 'Suzuki', 'car', 1),
    (22, 'Mitsubishi', 'car', 1),
    (23, 'Subaru', 'car', 1),
    (24, 'Fiat', 'car', 1),
    (25, 'Jeep', 'car', 1),
    (26, 'RAM', 'car', 1),
    (27, 'Dodge', 'car', 1),
    (28, 'GMC', 'car', 1);

-- Brands: motorcycles
INSERT OR IGNORE INTO brand (id, name, vehicle_type, is_seeded) VALUES
    (101, 'Honda', 'motorcycle', 1),
    (102, 'Yamaha', 'motorcycle', 1),
    (103, 'Suzuki', 'motorcycle', 1),
    (104, 'Kawasaki', 'motorcycle', 1),
    (105, 'KTM', 'motorcycle', 1),
    (106, 'Royal Enfield', 'motorcycle', 1),
    (107, 'BMW', 'motorcycle', 1),
    (108, 'TVS', 'motorcycle', 1),
    (109, 'Bajaj', 'motorcycle', 1),
    (110, 'Hero', 'motorcycle', 1),
    (111, 'Italika', 'motorcycle', 1),
    (112, 'Vento', 'motorcycle', 1),
    (113, 'Harley-Davidson', 'motorcycle', 1),
    (114, 'Ducati', 'motorcycle', 1);

-- Models: Toyota
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (1, 'Corolla', 1),
    (1, 'RAV4', 1),
    (1, 'Camry', 1),
    (1, 'Yaris', 1),
    (1, 'Hilux', 1),
    (1, 'Avanza', 1),
    (1, 'Raize', 1),
    (1, 'Tacoma', 1);

-- Models: Honda (car)
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (2, 'Civic', 1),
    (2, 'CR-V', 1),
    (2, 'Accord', 1),
    (2, 'HR-V', 1),
    (2, 'Fit', 1),
    (2, 'BR-V', 1),
    (2, 'City', 1),
    (2, 'Pilot', 1);

-- Models: Ford
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (3, 'F-150', 1),
    (3, 'Explorer', 1),
    (3, 'Escape', 1),
    (3, 'Focus', 1),
    (3, 'Maverick', 1),
    (3, 'Ranger', 1),
    (3, 'Bronco Sport', 1),
    (3, 'Lobo', 1);

-- Models: Hyundai
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (4, 'Tucson', 1),
    (4, 'Elantra', 1),
    (4, 'Santa Fe', 1),
    (4, 'Creta', 1),
    (4, 'Grand i10', 1),
    (4, 'HB20', 1),
    (4, 'Venue', 1);

-- Models: Kia
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (5, 'K3', 1),
    (5, 'Sportage', 1),
    (5, 'Sorento', 1),
    (5, 'Rio', 1),
    (5, 'Seltos', 1),
    (5, 'Forte', 1),
    (5, 'K4', 1),
    (5, 'Niro', 1);

-- Models: Volkswagen
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (6, 'Virtus', 1),
    (6, 'Tiguan', 1),
    (6, 'Golf', 1),
    (6, 'Jetta', 1),
    (6, 'Passat', 1),
    (6, 'Taos', 1),
    (6, 'T-Cross', 1),
    (6, 'Teramont', 1),
    (6, 'Polo', 1);

-- Models: Nissan
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (7, 'Versa', 1),
    (7, 'NP300', 1),
    (7, 'Sentra', 1),
    (7, 'Altima', 1),
    (7, 'Rogue', 1),
    (7, 'March', 1),
    (7, 'Kicks', 1),
    (7, 'Frontier', 1),
    (7, 'X-Trail', 1);

-- Models: Chevrolet
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (8, 'Aveo', 1),
    (8, 'Onix', 1),
    (8, 'Silverado', 1),
    (8, 'Equinox', 1),
    (8, 'Malibu', 1),
    (8, 'Tracker', 1),
    (8, 'Groove', 1),
    (8, 'Captiva', 1),
    (8, 'S10 Max', 1);

-- Models: Tesla
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (9, 'Model Y', 1),
    (9, 'Model 3', 1),
    (9, 'Model S', 1),
    (9, 'Model X', 1);

-- Models: Mazda
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (10, 'Mazda3', 1),
    (10, 'Mazda2', 1),
    (10, 'CX-5', 1),
    (10, 'CX-30', 1),
    (10, 'CX-3', 1),
    (10, 'CX-50', 1),
    (10, 'BT-50', 1);

-- Models: BMW (car)
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (11, '3 Series', 1),
    (11, 'X3', 1),
    (11, 'X5', 1);

-- Models: Mercedes-Benz
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (12, 'C-Class', 1),
    (12, 'GLC', 1),
    (12, 'E-Class', 1);

-- Models: Audi
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (13, 'A4', 1),
    (13, 'Q5', 1),
    (13, 'A3', 1),
    (13, 'Q3', 1),
    (13, 'Q7', 1);

-- Models: Renault
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (14, 'Kwid', 1),
    (14, 'Duster', 1),
    (14, 'Stepway', 1),
    (14, 'Oroch', 1),
    (14, 'Logan', 1);

-- Models: Peugeot
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (15, '208', 1),
    (15, '2008', 1),
    (15, '3008', 1),
    (15, 'Rifter', 1),
    (15, 'Partner', 1);

-- Models: MG
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (16, 'MG5', 1),
    (16, 'ZS', 1),
    (16, 'HS', 1),
    (16, 'RX8', 1),
    (16, 'MG3', 1);

-- Models: Chirey
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (17, 'Tiggo 2 Pro', 1),
    (17, 'Tiggo 4 Pro', 1),
    (17, 'Tiggo 7 Pro', 1),
    (17, 'Tiggo 8 Pro', 1);

-- Models: SEAT
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (18, 'Ibiza', 1),
    (18, 'Arona', 1),
    (18, 'Leon', 1),
    (18, 'Ateca', 1),
    (18, 'Toledo', 1);

-- Models: Cupra
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (19, 'Formentor', 1),
    (19, 'Leon', 1),
    (19, 'Ateca', 1),
    (19, 'Born', 1);

-- Models: JAC
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (20, 'SEI2', 1),
    (20, 'SEI3', 1),
    (20, 'SEI4 Pro', 1),
    (20, 'Frison T8', 1),
    (20, 'E10X', 1);

-- Models: Suzuki (car)
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (21, 'Swift', 1),
    (21, 'Ignis', 1),
    (21, 'Jimny', 1),
    (21, 'Ertiga', 1),
    (21, 'Vitara', 1);

-- Models: Mitsubishi
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (22, 'L200', 1),
    (22, 'Xpander', 1),
    (22, 'Mirage G4', 1),
    (22, 'Outlander', 1),
    (22, 'Montero Sport', 1);

-- Models: Subaru
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (23, 'Forester', 1),
    (23, 'Outback', 1),
    (23, 'XV', 1),
    (23, 'WRX', 1);

-- Models: Fiat
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (24, 'Mobi', 1),
    (24, 'Pulse', 1),
    (24, 'Argo', 1),
    (24, 'Fastback', 1),
    (24, 'Strada', 1);

-- Models: Jeep
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (25, 'Renegade', 1),
    (25, 'Compass', 1),
    (25, 'Wrangler', 1),
    (25, 'Grand Cherokee', 1);

-- Models: RAM
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (26, '700', 1),
    (26, '1200', 1),
    (26, '1500', 1),
    (26, '2500', 1);

-- Models: Dodge
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (27, 'Attitude', 1),
    (27, 'Journey', 1),
    (27, 'Durango', 1);

-- Models: GMC
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (28, 'Sierra', 1),
    (28, 'Terrain', 1),
    (28, 'Acadia', 1),
    (28, 'Yukon', 1);

-- Models: Honda (motorcycle)
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (101, 'CB125F', 1),
    (101, 'CBR500R', 1),
    (101, 'PCX 160', 1),
    (101, 'Navi', 1),
    (101, 'CB190R', 1);

-- Models: Yamaha
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (102, 'MT-07', 1),
    (102, 'YZF-R3', 1),
    (102, 'NMAX 155', 1),
    (102, 'XSR155', 1),
    (102, 'FZ-S', 1);

-- Models: Suzuki (motorcycle)
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (103, 'GSX-S750', 1),
    (103, 'V-Strom 650', 1),
    (103, 'Gixxer 250', 1),
    (103, 'Burgman Street', 1);

-- Models: Kawasaki
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (104, 'Ninja 400', 1),
    (104, 'Z650', 1),
    (104, 'Versys 650', 1),
    (104, 'KLR 650', 1);

-- Models: KTM
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (105, 'Duke 390', 1),
    (105, 'RC 390', 1),
    (105, '390 Adventure', 1);

-- Models: Royal Enfield
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (106, 'Classic 350', 1),
    (106, 'Hunter 350', 1),
    (106, 'Himalayan', 1),
    (106, 'Interceptor 650', 1);

-- Models: BMW (motorcycle)
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (107, 'R 1250 GS', 1),
    (107, 'G 310 R', 1),
    (107, 'F 900 GS', 1),
    (107, 'S 1000 RR', 1);

-- Models: TVS
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (108, 'Apache RTR 160', 1),
    (108, 'Ronin', 1);

-- Models: Bajaj
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (109, 'Pulsar NS200', 1),
    (109, 'Dominar 400', 1);

-- Models: Hero
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (110, 'Splendor Plus', 1),
    (110, 'XPulse 200', 1),
    (110, 'Hunk 160R', 1);

-- Models: Italika
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (111, 'FT150', 1),
    (111, 'DM200', 1),
    (111, 'AT110', 1),
    (111, 'Vort-X 300', 1),
    (111, 'D150', 1);

-- Models: Vento
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (112, 'Rocketman 250', 1),
    (112, 'Nitrox 200', 1),
    (112, 'Crossmax 300', 1),
    (112, 'Streetrod 150', 1);

-- Models: Harley-Davidson
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (113, 'Sportster S', 1),
    (113, 'Iron 883', 1),
    (113, 'Street Glide', 1),
    (113, 'Pan America 1250', 1);

-- Models: Ducati
INSERT OR IGNORE INTO model (brand_id, name, is_seeded) VALUES
    (114, 'Monster', 1),
    (114, 'Scrambler Icon', 1),
    (114, 'Panigale V2', 1),
    (114, 'Multistrada V4', 1);

COMMIT;

