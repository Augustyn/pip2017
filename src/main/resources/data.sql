insert into PRODUCTS (name ,description,IMAGE_URL )
values
('pralka','Indesit IWSD51051CECOPL to wolnostojąca pralka, ładowana od frontu o głębokości 40 cm. ','https://8.allegroimg.com/s1024/056bae/281901a64b50bc69ba57e208ffd8/Pralka-Indesit-IWSD51051CECOPL-281901a64b50bc69ba57e208ffd8'),
('pralka','Cechy wolnostojącej pralki ładowanej od góry Whirlpool: kolor biały.  ','http://www.whirlpool.pl/digitalassets/Picture/web1000x1000/859334649050_1000x1000_perspective.jpg' ),
('pralka','Electrolux EWF11274BW to nowoczesna pralka wolnostojąca, ładowana od frontu, o pojemności 7 kg. e białym,  biały','https://e.allegroimg.com/s1024/007778/178c78194be8a525900078c45b7e/Pralka-Electrolux-EWF11274BW-przod'),
('kuchnia','Amica 57GEH2.33ZpPF(W) to biała wolnostojąca kuchnia gazowo-elektryczna, z piekarnikiem XXL, o mocy 2900 W i pojemności 65 l','https://2.allegroimg.com/s1024/006114/14d8fc794f9aa09946acc5c30e72/Kuchnia-wolnostojaca-Amica-57GEH2.33ZpPFW-57GEH2.33ZpPFW'),
('lodówka','Biała chłodziarko-zamrażarka Amica FK205.4 ma zamrażarkę znajdującą się w jej dolnej części. .','https://d.allegroimg.com/s1024/004902/eafa87eb44479fbada01afad438d/Chlodziarko-zamrazarka-Amica-FK205.4-przod'),
('lodówka','Chłodziarko-zamrażarka Amica FK338.6GBF to nowoczesne urządzenie o wysokości 185 cm.','https://2.allegroimg.com/s1024/00d435/572099be41efa18803c58b6aa0a2/Chlodziarko-zamrazarka-Amica-FK338.6GBF-Lodowka'),
('lodówka','Biała chłodziarko-zamrażarka Amica FK205.4 ma zamrażarkę znajdującą się w jej dolnej części.','https://d.allegroimg.com/s1024/004902/eafa87eb44479fbada01afad438d/Chlodziarko-zamrazarka-Amica-FK205.4-przod'),
('lodówka','Chłodziarko-zamrażarka Amica FK3296.4F to model wolnostojący, o wysokości 185 cm.','https://6.allegroimg.com/s1024/0015d6/f9edfa9641ed9f06639d88057506/Chlodziarko-zamrazarka-Amica-FK3296.3F-FK3296.3F'),
('lodówka','Chłodziarka Amica FM104.4 to wolnostojące urządzenie z komorą niskich temperatur.','https://8.allegroimg.com/s1024/0042ac/757a038e43adae2a24108ecedd98/Chlodziarka-Amica-FM104.4-Chlodziarka'),
('lodówka','Manta CLF050 to podblatowa chłodziarko-zamrażarka wolnostojąca o wysokości 49 cm, w kolorze białym. Posiada 2-letnią gwarancję producenta.','https://3.allegroimg.com/s1024/00d44a/09ecb53b43f3a5f823df04cac473/Chlodziarko-zamrazarka-Manta-CLF050-CLF050');

insert into KEYWORDS (word)
values
('TOP3'),
('TOP2'),
('TOP1'),
('Najtańszy'),
('Najdroższy'),
('Najwydajniejszy'),
('Innowacyjy'),
('Funkcjonalny'),
('Wielozadaniowy'),
('Edycja limitowana'),
('Hit sezonu'),
('Rabat 10%'),
('Wyprzedaż'),
('Gwarancja 5 lat'),
('Cicha praca');

insert into PRODUCTS_KEYWORDS (PRODUCT_ID,KEYWORDS_ID)
values
(1,1),
(2,2),
(3,3),
(4,4),
(3,5),
(3,6),
(2,7),
(5,8),
(6,9),
(6,10),
(7,11),
(8,12),
(9,13),
(10,14),
(5,15);

