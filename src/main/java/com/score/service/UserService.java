package com.score.service;

import com.score.domain.dto.request.PaginatorRequestDTO;
import com.score.domain.dto.request.ScoreRequestDTO;
import com.score.domain.dto.response.PositionResponseDTO;
import com.score.domain.repository.User;
import com.score.repository.UserRepository;
import com.score.service.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@Service
public class UserService implements IUserService {

  private UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /** @param scoreRequest */
  @Override
  public void score(ScoreRequestDTO scoreRequest) {
    Mono.fromRunnable(
            () -> userRepository.score(scoreRequest.getUserId(), scoreRequest.getPoints()))
        .subscribe();
  }

  /**
   * @param userId
   * @return
   */
  @Override
  public Mono<PositionResponseDTO> position(Integer userId) {
    return Mono.defer(
        () -> {
          Optional<User> user = userRepository.getUser(userId);
          if (!user.isPresent()) {
            return Mono.error(new ResourceNotFoundException());
          }
          User u = user.get();
          OptionalInt position = userRepository.getPosition(userId);
          return Mono.just(
              new PositionResponseDTO(u.getUserId(), u.getScore(), position.getAsInt() + 1));
        });
  }

  /**
   * @param paginatorRequestDTO
   * @return
   */
  @Override
  public Flux<PositionResponseDTO> highscorelist(PaginatorRequestDTO paginatorRequestDTO) {
    return Flux.defer(
        () -> {
          List<User> users =
              userRepository.getUsers(paginatorRequestDTO.getPage(), paginatorRequestDTO.getSize());
          return Flux.fromStream(
              IntStream.range(0, users.size())
                  .mapToObj(
                      i ->
                          new PositionResponseDTO(
                              users.get(i).getUserId(), users.get(i).getScore(), i + 1)));
        });
  }
}
