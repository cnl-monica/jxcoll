--CREATE LANGUAGE plpgsql;

-- create function and trigger for unique values filling the unique tables
--
    CREATE OR REPLACE FUNCTION process_filter() RETURNS TRIGGER AS $filter$
    DECLARE
    send_dwh BOOLEAN;
    return_v varchar;
    sql_query varchar;
    v_error varchar;
    CONNECTION varchar;
    time timestamp;
    startTime varchar;
    endTime varchar;

    BEGIN
            --
            -- Create a rows in unique tables to reflect the operation performed on records_main,
            --
	    -- if operation is insert
	    IF (TG_OP = 'INSERT') THEN
			IF (NEW.flowendreason = 1) THEN
                	BEGIN
      		  -- Pripojenie na databazu
		      CONNECTION := CURRENT_TIMESTAMP;
		      PERFORM dblink_connect(CONNECTION, 'hostaddr=127.0.0.1 dbname=bmdwh port=5432 user=bm password=bm');
			    ----------------------------SourPort------------------------------------
			    BEGIN
				    -- try to insert value XY into unique_XY table
	        			INSERT INTO uniqueSourceTransportPort SELECT NEW.sourceTransportPort, 0;
				    -- if the value XY is already existing in XY unique table
				    EXCEPTION WHEN UNIQUE_VIOLATION THEN
				       	-- catch the exception and increment the XY value's counter
	       				UPDATE uniqueSourceTransportPort
					    SET duplicateCount = duplicateCount+1
					    WHERE (SELECT sourceTransportPort = NEW.sourceTransportPort);
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------ipSour--------------------------------------
			    BEGIN
				    send_dwh := TRUE;
	        			INSERT INTO uniqueSourceIPv4Address SELECT NEW.sourceIPv4Address, 0;
				    EXCEPTION WHEN UNIQUE_VIOLATION THEN
					    send_dwh := FALSE;
			       		UPDATE uniqueSourceIPv4Address
					    SET duplicateCount = duplicateCount+1
					    WHERE (SELECT sourceIPv4Address = NEW.sourceIPv4Address);
			    END;
			    BEGIN
				    IF (send_dwh = TRUE) THEN

					    sql_query := 'INSERT INTO ip (ip) '|| 'VALUES (''' || NEW.sourceIPv4Address || ''')';
					    BEGIN
			      		SELECT INTO return_v * FROM dblink_exec(CONNECTION, sql_query, true);
					    EXCEPTION WHEN others THEN
					    RAISE NOTICE '[DWH load function] Source IP address already exists';
					    END;

					    --get the error message
					    --SELECT INTO v_error * FROM dblink_error_message(CONNECTION);

					    --IF position('ERROR' in v_error) > 0 OR position('WARNING' in v_error) > 0
					    --	THEN RAISE EXCEPTION '%', v_error;
	      				--END IF;
				    END IF;
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------DestPort------------------------------------
			    BEGIN
	        			INSERT INTO uniqueDestinationTransportPort SELECT NEW.destinationTransportPort, 0;
				    EXCEPTION WHEN UNIQUE_VIOLATION THEN
	       				UPDATE uniqueDestinationTransportPort
					    SET duplicateCount = duplicateCount+1
					    WHERE (SELECT destinationTransportPort = NEW.destinationTransportPort);
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------ipDest--------------------------------------
			    BEGIN
				    send_dwh := TRUE;
	        			INSERT INTO uniqueDestinationIPv4Address SELECT NEW.destinationIPv4Address, 0;
				    EXCEPTION WHEN UNIQUE_VIOLATION THEN
				       	send_dwh := FALSE;
				      	UPDATE uniqueDestinationIPv4Address
					    SET duplicateCount = duplicateCount+1
					    WHERE (SELECT destinationIPv4Address = NEW.destinationIPv4Address);
			    END;
			    BEGIN
				    IF (send_dwh = TRUE) THEN
					    sql_query := 'INSERT INTO ip (ip) '|| 'VALUES (''' || NEW.destinationIPv4Address || ''')';
					    BEGIN
					    SELECT INTO return_v * FROM dblink_exec(CONNECTION, sql_query, true);
					    EXCEPTION WHEN others THEN
					    RAISE NOTICE '[DWH load function] Destination IP address already exists';
					    END;

					    --get the error message
					    --SELECT INTO v_error * FROM dblink_error_message(CONNECTION);

					    --IF position('ERROR' in v_error) > 0 OR position('WARNING' in v_error) > 0
					    --	THEN RAISE EXCEPTION '%', v_error;
	      				--END IF;
				    END IF;
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------ObsPoID-------------------------------------
			    BEGIN
				    send_dwh := TRUE;
	        			INSERT INTO uniqueObservationPointId SELECT NEW.observationPointId, 0;
				    EXCEPTION WHEN UNIQUE_VIOLATION THEN
					    send_dwh := FALSE;
	       				UPDATE uniqueObservationPointId
					    SET duplicateCount = duplicateCount+1
					    WHERE (SELECT observationPointId = NEW.observationPointId);
			    END;
			    BEGIN
				    IF (send_dwh = TRUE) THEN
					    sql_query := 'INSERT INTO observation_point (observation_point) '|| 'VALUES (''' ||
							    NEW.observationPointId || ''')';

					    SELECT INTO return_v * FROM dblink_exec(CONNECTION, sql_query, false);

					    --get the error message
					    --SELECT INTO v_error * FROM dblink_error_message(CONNECTION);

					    --IF position('ERROR' in v_error) > 0 OR position('WARNING' in v_error) > 0
					    --	THEN RAISE EXCEPTION '%', v_error;
	      				--END IF;
				    END IF;
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------startTime-----------------------------------
			    BEGIN
				   -- IF (NEW.flowendreason = 1) THEN
				        time:= to_timestamp(NEW.flowstartmilliseconds/1000);
					
					    sql_query := 'INSERT INTO time(minute,hour,day,month,year) '|| '(select ''' ||
							date_part('minute', time)|| ''' as minute,''' ||
							date_part('hour', time)|| ''' as hour,''' ||
							date_part('day', time)|| ''' as day,''' ||
							date_part('month', time)|| ''' as month,'''||
							date_part('year',time)|| ''' as year where not exists(select * from time where minute='''||
							date_part('minute', time)|| ''' AND hour= '''||
							date_part('hour', time)|| ''' AND day= '''||
							date_part('day', time)|| ''' AND month= '''||
							date_part('month', time)||''' AND year='''||
							date_part('year', time)||'''))';
					    SELECT INTO return_v * FROM dblink_exec(CONNECTION, sql_query, false);

					    sql_query :='select id_time from time where minute='''||
							date_part('minute', time)|| ''' AND hour= '''||
							date_part('hour', time)|| ''' AND day= '''||
							date_part('day', time)|| ''' AND month= '''||
							date_part('month', time)||''' AND year='''||
							date_part('year', time)||'''';
					    --SELECT * FROM dblink_send_query(CONNECTION, sql_query);
					    SELECT * INTO startTime FROM dblink(CONNECTION, sql_query) AS (id_time bigint);


					    --get the error message
					    SELECT INTO v_error * FROM dblink_error_message(CONNECTION);

					    IF position('ERROR' in v_error) > 0 OR position('WARNING' in v_error) > 0
						    THEN RAISE EXCEPTION '%', v_error;
	      				END IF;
				  --  END IF;
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------endTime-----------------------------------
			    BEGIN
				   -- IF (NEW.flowendreason = 1) THEN
				        time:= to_timestamp(NEW.flowendmilliseconds/1000);
					
					    sql_query := 'INSERT INTO time(minute,hour,day,month,year) '|| '(select ''' ||
							    date_part('minute', time)|| ''' as minute,''' ||
							    date_part('hour', time)|| ''' as hour,''' ||
							    date_part('day', time)|| ''' as day,''' ||
							    date_part('month', time)|| ''' as month,'''||
							    date_part('year',time)|| ''' as year where not exists(select * from time where minute='''||
							    date_part('minute', time)|| ''' AND hour= '''||
							    date_part('hour', time)|| ''' AND day= '''||
							    date_part('day', time)|| ''' AND month= '''||
							    date_part('month', time)||''' AND year='''||
							    date_part('year', time)||'''))';
					    SELECT INTO return_v * FROM dblink_exec(CONNECTION, sql_query, false);

					    sql_query :='select id_time from time where minute='''||
							date_part('minute', time)|| ''' AND hour= '''||
							date_part('hour', time)|| ''' AND day= '''||
							date_part('day', time)|| ''' AND month= '''||
							date_part('month', time)||''' AND year='''||
							date_part('year', time)||'''';
					   -- SELECT * FROM dblink_send_query(CONNECTION, sql_query);
					   -- SELECT INTO endTime * FROM dblink_get_result(CONNECTION);
					   SELECT * INTO endTime FROM dblink(CONNECTION, sql_query) AS (id_time bigint);

					    --get the error message
					    --SELECT INTO v_error * FROM dblink_error_message(CONNECTION);

					    --IF position('ERROR' in v_error) > 0 OR position('WARNING' in v_error) > 0
						    --THEN RAISE EXCEPTION '%', v_error;
	      				--END IF;
				   -- END IF;
			    END;
			    ------------------------------------------------------------------------
			    ----------------------------flow----------------------------------------
			    BEGIN

				sql_query := 'INSERT INTO flow(packettotalcount, octettotalcount, id_observation_point_fk, id_ip_source_fk, id_ip_destination_fk, id_time_start_fk,id_time_end_fk) VALUES (''' ||
								NEW.packettotalcount || ''',''' ||
								NEW.octettotalcount || ''',''' ||
								NEW.observationpointid || ''',''' ||
								NEW.sourceipv4address || ''',''' ||
								NEW.destinationipv4address || ''',''' ||
								startTime || ''',''' ||
								endTime ||''')';

			        SELECT INTO return_v * FROM dblink_exec(CONNECTION, sql_query, false);
				--get the error message
				SELECT INTO v_error * FROM dblink_error_message(CONNECTION);

				IF position('ERROR' in v_error) > 0 OR position('WARNING' in v_error) > 0
				    THEN RAISE EXCEPTION '%', v_error;
	      			END IF;
			    END;
			    ------------------------------------------------------------------------
	        	  BEGIN
		      		PERFORM dblink_disconnect(CONNECTION);
	      	  		EXCEPTION
		      		WHEN others THEN
		      			PERFORM dblink_disconnect(CONNECTION);
		      			RAISE EXCEPTION '(%)', SQLERRM;
		      	END;   
		    END;
		   END IF;
                	--RETURN NEW;
            END IF;
            RETURN NULL; -- result is ignored since this is an AFTER trigger

        END;
    $filter$ LANGUAGE plpgsql;

CREATE TRIGGER filter
AFTER INSERT ON records_main
    FOR EACH ROW EXECUTE PROCEDURE process_filter();
