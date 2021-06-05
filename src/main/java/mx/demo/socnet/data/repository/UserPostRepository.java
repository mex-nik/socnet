package mx.demo.socnet.data.repository;

import mx.demo.socnet.data.entity.UserPost;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 05.06.2021
 * @project socnet
 */
public interface UserPostRepository extends PagingAndSortingRepository<UserPost, Long> {
}
