package com.ModbusConnector.repository;

import com.ModbusConnector.Stend;
import com.ModbusConnector.model.TableReports;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
public interface TableReportsRepository extends CrudRepository<TableReports, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO `stend_resources`.`table_reports` " +
            "(`actual_force`, `author_id`, `date_time`, `max_deformation`, `number_act`, `number_drawing`, `ost_deformation`, `required_force`, `valid`) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9);", nativeQuery = true)
    void addReport(@Param("actual_force") Float actual_force, @Param("author") int authorId, @Param("date_time") Date date_time,
                    @Param("max_deformation") Float max_deformation, @Param("number_act") int number_act, @Param("number_drawing") Integer number_drawing,
                    @Param("ost_deformation") Float ost_deformation, @Param("required_force") Float required_force, @Param("valid") int valid);

    @Query(value="SELECT * FROM stend_resources.table_reports", nativeQuery = true)
    public List<TableReports> stendInfo();

}
