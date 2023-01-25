-- -----------------------------------------------------
-- Function `belexo`.`SpanMatch`
-- The SpanMatch function tells whether an annotation
-- features a span (of its potentially multiple)
-- matching the area.
-- -----------------------------------------------------

DROP FUNCTION IF EXISTS `belexo`.`SpanMatch`;;

CREATE FUNCTION `belexo`.`SpanMatch`(
       `annotation` INT,
       `lft` INT,
       `rgt` INT
) RETURNS INT
DETERMINISTIC
BEGIN
	DECLARE ret INT;
	SET ret = 0;
	SELECT count(a.id) INTO ret FROM `belexo`.`ann_spans`as a WHERE a.ann=annotation AND a.`begin`<=lft AND a.`end`>=rgt LIMIT 1;

	RETURN ret;
END;;

-- -----------------------------------------------------
-- Function `belexo`.`TokenMatch`
-- The SpanMatch function tells whether an annotation
-- features a span (of its potentially multiple)
-- matching a token's.
-- -----------------------------------------------------

DROP FUNCTION IF EXISTS `belexo`.`TokenMatch`;;

CREATE FUNCTION `belexo`.`TokenMatch`(
       `annotation` INT,
       `token` INT
) RETURNS INT
DETERMINISTIC
BEGIN
	DECLARE ret INT;
	SET ret = 0;
	SELECT count(a.id) INTO ret FROM `belexo`.`ann_spans`as a, `belexo`.`tokens` as t, `belexo`.`annotations` as ann WHERE (t.id=token AND ann.id=annotation and t.node=ann.node AND a.ann=annotation AND ((a.`begin`<=t.`begin` AND t.`begin`<=t.`end`) OR ((a.`begin`<=t.`end`) AND (t.`end`<=a.`end`)) OR ((t.`begin`<=a.`begin`) AND (a.`begin`<=t.`end`)) OR ((t.`begin`<=a.`end`) AND (a.`end`<=t.`end`)))) LIMIT 1;
    RETURN ret;
END;;
