package mx.demo.socnet.data.repository;

import mx.demo.socnet.data.entity.UserData;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 04.06.2021
 * @project socnet
 */
@Repository
public interface UserDataRepository extends PagingAndSortingRepository<UserData, Long> {
}
