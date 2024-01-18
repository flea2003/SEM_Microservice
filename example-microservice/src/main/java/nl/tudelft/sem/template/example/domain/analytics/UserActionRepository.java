package nl.tudelft.sem.template.example.domain.analytics;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Integer> {

    /**
     * An auto-implemented method which queries the total number of rows in the table with a certain type value.
     *
     * @param type the type to filter by
     * @return the number of rows with this type value
     */
    long countByType(String type);

    /**
     * An query method which will return a list of action types, ordered by descending frequency.
     *
     * @return a list of Object[] where [0] is the action type, and [1] is its frequency, in descending order of frequency
     */
    @Query("SELECT a.type, COUNT(a) as frequency FROM UserAction a GROUP BY a.type ORDER BY frequency DESC")
    List<Object[]> getActionsByTypeFrequency();

}
