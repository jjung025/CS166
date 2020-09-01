--1
SELECT COUNT(*)
FROM part_nyc N
WHERE N.on_hand > 70;

--2
SELECT SUM(N_UNION_S.on_hand)
FROM (SELECT N.on_hand 
	FROM part_nyc N
	WHERE N.color IN (SELECT color_id FROM color WHERE color_name = 'Red') 
	UNION 
	SELECT S.part_number
	FROM part_sfo S
	WHERE S.color IN (SELECT color_id FROM color WHERE color_name = 'RED')) 
	AS N_UNION_S;

--3
SELECT Su.supplier_name
FROM supplier Su 
WHERE (SELECT SUM(N.on_hand)
	FROM part_nyc N
	WHERE N.on_hand 
	>
	SELECT SUM(S.on_hand )
	FROM part_sfo S 
	WHERE S.on_hand);

—-4
SELECT Su.supplier_name
FROM supplier Su
WHERE (SELECT 
	FROM 
	WHERE
	- 
	SELECT 
	FROM
	WHERE


-—5 
UPDATE part_nyc N
SET N.on_hand = N.on_hand - 10;

—-6
DELETE
FROM part_nyc N 
WHERE N.on_hand < 30;

--1 7854
--2 330
--3 6 or 10
--4 10
--5 49995
--6 8456