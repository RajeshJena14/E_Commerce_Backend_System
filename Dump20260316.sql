-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: inctureecommercedatabase
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint NOT NULL,
  `total_price` double NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9emlp6m95v5er2bcqkjsw48he` (`user_id`),
  CONSTRAINT `FKg5uhi8vpsuy0lgloxk2h4w5o6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (202,1130,1);
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL,
  `quantity` int NOT NULL,
  `cart_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK99e0am9jpriwxcm6is7xfedy3` (`cart_id`),
  KEY `FK1re40cjegsfvw58xrkdp6bac6` (`product_id`),
  CONSTRAINT `FK1re40cjegsfvw58xrkdp6bac6` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK99e0am9jpriwxcm6is7xfedy3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (252,1,202,5),(253,2,202,22);
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items_seq`
--

DROP TABLE IF EXISTS `cart_items_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items_seq`
--

LOCK TABLES `cart_items_seq` WRITE;
/*!40000 ALTER TABLE `cart_items_seq` DISABLE KEYS */;
INSERT INTO `cart_items_seq` VALUES (1151);
/*!40000 ALTER TABLE `cart_items_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_seq`
--

DROP TABLE IF EXISTS `cart_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_seq`
--

LOCK TABLES `cart_seq` WRITE;
/*!40000 ALTER TABLE `cart_seq` DISABLE KEYS */;
INSERT INTO `cart_seq` VALUES (751);
/*!40000 ALTER TABLE `cart_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL,
  `price` double NOT NULL,
  `quantity` int NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (302,160,1,252,14),(303,590,2,252,17),(352,380,1,302,19),(353,120,1,303,3),(402,90,2,352,8),(452,295,1,402,17),(502,2500,1,452,18),(503,2500,1,453,18);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items_seq`
--

DROP TABLE IF EXISTS `order_items_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items_seq`
--

LOCK TABLES `order_items_seq` WRITE;
/*!40000 ALTER TABLE `order_items_seq` DISABLE KEYS */;
INSERT INTO `order_items_seq` VALUES (601);
/*!40000 ALTER TABLE `order_items_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL,
  `order_date` datetime(6) NOT NULL,
  `order_status` varchar(255) NOT NULL,
  `payment_status` varchar(255) NOT NULL,
  `total_amount` double NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK32ql8ubntj5uh44ph9659tiih` (`user_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (252,'2026-03-12 23:04:17.527124','SHIPPED','SUCCESSFUL',750,2),(302,'2026-03-12 23:17:33.375310','CANCELLED','SUCCESSFUL',380,3),(303,'2026-03-12 23:20:46.272078','PLACED','SUCCESSFUL',120,2),(352,'2026-03-13 19:45:52.577340','PLACED','SUCCESSFUL',90,402),(402,'2026-03-13 19:50:16.944718','PLACED','SUCCESSFUL',295,402),(452,'2026-03-15 22:16:23.290420','FAILED','FAILED',2500,452),(453,'2026-03-15 22:17:00.581900','SHIPPED','SUCCESSFUL',2500,452);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders_seq`
--

DROP TABLE IF EXISTS `orders_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders_seq`
--

LOCK TABLES `orders_seq` WRITE;
/*!40000 ALTER TABLE `orders_seq` DISABLE KEYS */;
INSERT INTO `orders_seq` VALUES (551);
/*!40000 ALTER TABLE `orders_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL,
  `category` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `rating` int NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (3,'Analog','Minimalist leather band watch for everyday wear.','https://th.bing.com/th/id/OIP.FoK1hPo_pVnA41aR_MLT2QHaIB?w=182&h=197&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Classic Oxford Analog',120,4,40),(4,'Chronograph','Sporty chronograph with stainless steel strap and stopwatch features.','https://th.bing.com/th/id/OIP.Oi9r_Q2fR5w_6DrnvNtMlAHaIr?w=152&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Velocity Chronograph',250,5,30),(5,'Diver','Professional diving watch with luminous hands and rotating bezel.','https://th.bing.com/th/id/OIP.RIw_uBI6T9mt0HMNruv6ZAHaFj?w=247&h=185&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','AquaDiver Pro 300m',450,5,15),(6,'Smartwatch','Advanced smartwatch with heart rate monitor, GPS, and notifications.','https://th.bing.com/th/id/OIP.P13p5a7wnHs1om9t9OEUOAHaFh?w=272&h=203&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','TechPulse Smartwatch',199.99,4,100),(7,'Automatic','Self-winding mechanical watch with a skeleton dial exposing the movement.','https://th.bing.com/th/id/OIP.Hjzb7zA5FOWYIqLrhcXo9wHaE8?w=300&h=200&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Heritage Automatic',550,5,20),(8,'Digital','Sleek all-black digital watch with alarm and backlight.','https://th.bing.com/th/id/OIP.tPQoM24AA4FBSn-3y2xLAgHaHa?w=141&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Midnight Onyx Digital',45,4,147),(9,'Luxury','Women\'s luxury watch with a diamond-studded bezel.','https://th.bing.com/th/id/OIP.CmHWt5KczYXyfzceCAJN5wHaHa?w=161&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Rose Gold Elegance',320,5,25),(10,'Field','Rugged, lightweight field watch with a durable nylon nato strap.','https://th.bing.com/th/id/OIP.k7f94uJ2yiRwe8Wj92x3KgHaFy?w=263&h=206&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Titanium Field Watch',180,4,60),(11,'Aviation','Features a slide rule bezel and dual time zone display.','https://th.bing.com/th/id/OIP.nEoAXTXSJw-mZRIEPSd3SAHaHa?w=181&h=181&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Pilot\'s Aviation Master',410,5,18),(12,'Digital','Vintage style digital watch with a built-in calculator.','https://th.bing.com/th/id/OIP.g4Daj8L4OiGy_vm5_eFqBwAAAA?w=175&h=185&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Retro 80s Calculator',35,4,200),(13,'Analog','Never needs a battery replacement, charges continuously in any light.','https://th.bing.com/th/id/OIP.Zn4-yZ9K3xx4LJ992M4_sgHaHa?w=188&h=188&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Solar Powered Eco-Drive',210,5,39),(14,'Analog','Ultra-thin profile featuring scratch-resistant sapphire glass.','https://th.bing.com/th/id/OIP.Gq35XPeEmIWPwQ163mDF4gHaHa?w=174&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Sapphire Crystal Minimalist',160,4,53),(15,'Chronograph','Water-resistant chronograph with a stunning blue sunburst dial.','https://th.bing.com/th/id/OIP.4vp1J_N8ycz55UVqXSRwYAHaFL?w=270&h=189&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Deep Blue Ocean Chrono',275,5,31),(16,'Smartwatch','Slim profile fitness tracker offering step counting and sleep analysis.','https://th.bing.com/th/id/OIP.747anXrr8dQ3ucQjA-hj4AHaJn?w=89&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Fitness Tracker V2',159.99,3,120),(17,'Dress','Heavy, highly polished dress watch perfect for formal occasions.','https://th.bing.com/th/id/OIP.gLV4VWoo4uCMUr2iOo6VFAHaNK?w=115&h=196&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Tungsten Carbide Dress Watch',295,4,19),(18,'Luxury','Exquisite craftsmanship featuring a mesmerizing tourbillon mechanism.','https://th.bing.com/th/id/OIP.mocdhinI7Efv9qvDTKRJLQHaE8?w=258&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Tourbillon Grand Complication',2500,5,10),(19,'Aviation','Equipped with tritium gas tubes for a constant glow without charging.','https://th.bing.com/th/id/OIP.VPxvOACSHPuEmFRukxxQ8QHaHa?w=202&h=203&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Luminous Night Pilot',380,4,31),(20,'Digital','Waterproof and shock-resistant watch designed specifically for children.','https://th.bing.com/th/id/OIP.vhtYMz4hD_bDGOKu1ujXOwHaHa?w=178&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Kids Colorful Digital',25,4,300),(21,'Pocket','Classic mechanical pocket watch complete with an antique chain.','https://th.bing.com/th/id/OIP.QnT-Oz-_GrasCdA2z-j7nQHaHa?w=187&h=188&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Vintage Pocket Watch',110,4,15),(22,'Chronograph','Motorsport-inspired design showcasing a real carbon fiber dial.','https://th.bing.com/th/id/OIP.aNBKZ0ulj4kvbAl82fh4uQHaIt?w=114&h=180&c=7&r=0&o=7&dpr=1.3&pid=1.7&rm=3','Carbon Fiber Racing Watch',340,5,32);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products_seq`
--

DROP TABLE IF EXISTS `products_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products_seq`
--

LOCK TABLES `products_seq` WRITE;
/*!40000 ALTER TABLE `products_seq` DISABLE KEYS */;
INSERT INTO `products_seq` VALUES (151);
/*!40000 ALTER TABLE `products_seq` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK3g1j96g94xpk3lpxl2qbl985x` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'rkjrajesh2004@gmail.com','Rajesh','$2a$12$u0h95Abzv2vkUoqsLflYquZr9SZCalcf/C2Dbpm64hcxpPnfZzX3q','ADMIN'),(2,'sharvanimeher9@gmail.com','Sharvani','$2a$12$0hOWdNdWUpoJsCjIlZEcQucZDbpIQ3sNVMgLT/SDh24XXF7zZaFn2','CUSTOMER'),(3,'sankarn3251@gmail.com','Sankar','$2a$12$d4Ej0avXhS4lvqMSIX1q7eMLoozKl/IRYKgXM4qMCibV0cjh/Sz7C','CUSTOMER'),(4,'a.kiran2543@gmail.com','Aditya','$2a$12$R9vGND9g4bjGYBzhBsCDTeMPqkYUScTJEnAAInN8bvtTILMs3iLnK','CUSTOMER'),(5,'mjena2005@gmail.com','Monalisa','$2a$12$PruEVGdb6dYl7HQ1aCErbe395rOnolghOCgj/73i4r8TZYELrvBQ2','CUSTOMER'),(402,'dayajena123@gmail.com','Swati','$2a$12$z6pR.E/Duf54D/ycNYno5.nmbP74cRqfgX.867Ky34QIY1JXHfH7e','CUSTOMER'),(452,'namitajena8116@gmail.com','Namita','$2a$12$3j0VCEBmyYXOe4K5.3ZBTeFyRhlcsqRKaX/DxHSkxvoTbmtDGBHSe','CUSTOMER');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users_seq`
--

DROP TABLE IF EXISTS `users_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users_seq`
--

LOCK TABLES `users_seq` WRITE;
/*!40000 ALTER TABLE `users_seq` DISABLE KEYS */;
INSERT INTO `users_seq` VALUES (551);
/*!40000 ALTER TABLE `users_seq` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-16 16:21:18
