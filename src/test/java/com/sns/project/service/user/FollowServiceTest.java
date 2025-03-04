package com.sns.project.service.user;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class FollowServiceTest {
//
//  @Mock
//  private UserService userService;
//
//  @Mock
//  private FollowRepository followRepository;
//
//  @InjectMocks
//  private FollowingService followingService;
//
//
//  @BeforeEach
//  void setUp() {
//    MockitoAnnotations.openMocks(this);
//  }
//
//  @Test
//  void testFollowUser() {
//    Long followerId = 1L;
//    Long followingId = 2L;
//    User follower = new User(followerId);
//    User following = new User(followingId);
//
//    when(userService.getUserById(followerId)).thenReturn(follower);
//    when(userService.getUserById(followingId)).thenReturn(following);
//    when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);
//
//    followingService.followUser(followerId, followingId);
//
//    verify(followRepository, times(1)).save(any(Follow.class));
//  }
//
//  @Test
//  void testUnfollowUser() {
//    User following = new User(1L);
//    User follower = new User(2L);
//
//    when(userService.getUserById(follower.getId())).thenReturn(follower);
//    when(userService.getUserById(following.getId())).thenReturn(following);
//    when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);
//
//    followingService.unfollowUser(follower.getId(), following.getId());
//
//    verify(followRepository, times(1)).deleteByFollowerAndFollowing(follower, following);
//  }
//
//
//  @Test
//  void testGetAllFollowings() {
//  }
//
//  @Test
//  void testGetAllFollowers() {
//    User following = new User(1L);
//    User follower = new User(2L);
//    User follower2 = new User(3L);
//
//    List<Follow> followerList = Arrays.asList(new Follow(follower, following),
//        new Follow(follower2, following));
//    Pageable pageable = PageRequest.of(0, 10);
//    Page<Follow> page = new PageImpl<>(followerList);
//
//    when(followRepository.findAllByFollowingId(following.getId(), pageable))
//        .thenReturn(page);
//
//    FollowersResponse response = followingService.getAllFollowers(following.getId(), 0, 10);
//
//    assertNotNull(response);
//    assertEquals(2, response.getFollowers().size());
//
//  }


}