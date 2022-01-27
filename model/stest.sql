DELIMITER $$

DROP FUNCTION IF EXISTS myfun;

CREATE FUNCTION myfun( `t` VARCHAR(100) ) RETURNS INT
DETERMINISTIC
BEGIN
	DECLARE ret INT;
	SET ret = 0;
	SELECT count(*) INTO ret from test;
	return ret;
END$$
DELIMITER ;
