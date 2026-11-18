package com.halimjr11.genres

import app.cash.turbine.test
import com.halimjr11.common.NetworkResult
import com.halimjr11.domain.model.Genre
import com.halimjr11.domain.usecase.GetGenresUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@OptIn(ExperimentalCoroutinesApi::class)
class GenreListViewModelTest {
    private val useCase = mockk<GetGenresUseCase>()

    // runTest shares this scheduler, so the ViewModel's launch runs while Turbine awaits
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterEach fun tearDown() = Dispatchers.resetMain()

    // Each scenario must emit Loading first, then its final state
    @ParameterizedTest(name = "{argumentSetName}")
    @MethodSource("scenarios")
    fun emitsLoadingThenFinalState(
        result: NetworkResult<List<Genre>>,
        expected: GenreListUiState
    ) = runTest(dispatcher) {
        coEvery { useCase() } returns result
        GenreListViewModel(useCase).uiState.test {
            assertTrue(awaitItem().isLoading, "first state must be Loading")
            assertEquals(expected, awaitItem())
            expectNoEvents()
        }
    }

    @Test fun retryAfterErrorGoesLoadingThenSuccess() = runTest(dispatcher) {
        coEvery { useCase() } returns NetworkResult.Error("offline")
        val vm = GenreListViewModel(useCase)
        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertEquals(GenreListViewModel.ERROR_MESSAGE, awaitItem().error)

            coEvery { useCase() } returns NetworkResult.Success(listOf(action))
            vm.load()
            assertTrue(awaitItem().isLoading)
            assertEquals(listOf(action), awaitItem().genres)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 2) { useCase() }
    }

    // Search field: case-insensitive "contains" on the genre name; blank shows everything
    @ParameterizedTest(name = "[{index}] query \"{0}\" -> {1}")
    @CsvSource(
        value = [
            "NULL, 'Action|Adventure|Drama|Science Fiction'",
            "act, Action",
            "ADV, Adventure",
            "'  fi  ', Science Fiction",
            "a, 'Action|Adventure|Drama'",
            "zzz, ''",
        ],
        nullValues = ["NULL"]
    )
    fun searchFiltersGenresByName(query: String?, expected: String) {
        val state = GenreListUiState(isLoading = false, genres = all, query = query.orEmpty())
        assertEquals(expected.split("|").filter { it.isNotEmpty() }, state.visibleGenres.map { it.name })
    }

    @Test fun queryUpdatesStateAndSurvivesRetry() = runTest(dispatcher) {
        coEvery { useCase() } returns NetworkResult.Error("offline")
        val vm = GenreListViewModel(useCase)
        vm.uiState.test {
            awaitItem() // loading
            awaitItem() // error
            vm.onQueryChange("dra")
            assertEquals("dra", awaitItem().query)

            coEvery { useCase() } returns NetworkResult.Success(all)
            vm.load()
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertEquals("dra", loaded.query)
            assertEquals(listOf("Drama"), loaded.visibleGenres.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private val action = Genre(28, "Action")
        private val all = listOf(action, Genre(12, "Adventure"), Genre(18, "Drama"), Genre(878, "Science Fiction"))

        @JvmStatic fun scenarios(): Stream<Arguments> = Stream.of(
            Arguments.argumentSet("use case returns 1 genre -> Loading, then genres shown", NetworkResult.Success(listOf(action)), GenreListUiState(false, listOf(action), null)),
            Arguments.argumentSet("use case fails (offline) -> Loading, then error message", NetworkResult.Error("offline"), GenreListUiState(false, emptyList(), GenreListViewModel.ERROR_MESSAGE)),
            Arguments.argumentSet("use case returns empty list -> Loading, then empty message", NetworkResult.Success(emptyList<Genre>()), GenreListUiState(false, emptyList(), GenreListViewModel.EMPTY_MESSAGE)),
        )
    }
}
