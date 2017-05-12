import com.bubelov.coins.BuildConfig;
import com.bubelov.coins.dagger.Injector;
import com.bubelov.coins.dagger.MainComponent;
import com.bubelov.coins.data.repository.placecategory.PlaceCategoriesDataSourceDb;
import com.bubelov.coins.data.repository.placecategory.PlaceCategoriesDataSourceMemory;
import com.bubelov.coins.data.repository.placecategory.PlaceCategoriesDataSourceNetwork;
import com.bubelov.coins.data.repository.placecategory.PlaceCategoriesRepository;
import com.bubelov.coins.domain.PlaceCategory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Random;

/**
 * @author Igor Bubelov
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class PlaceCategoriesRepositoryTest {
    private static final long TEST_CATEGORY_ID = 1;

    private PlaceCategoriesRepository repository;

    private PlaceCategoriesDataSourceNetwork networkSource;

    private PlaceCategoriesDataSourceDb dbSource;

    private PlaceCategoriesDataSourceMemory memorySource;

    @Before
    public void setUp() {
        Injector.INSTANCE.initMainComponent(RuntimeEnvironment.application);
        MainComponent mainComponent = Injector.INSTANCE.mainComponent();
        repository = mainComponent.placeCategoriesRepository();
        networkSource = mainComponent.placeCategoriesDataSourceNetwork();
        dbSource = mainComponent.placeCategoriesDataSourceDb();
        memorySource = mainComponent.placeCategoriesDataSourceMemory();
    }

    @Test
    public void memorySource_addsCategory() {
        PlaceCategory category = generateRandomCategory();
        Assert.assertNull(memorySource.getPlaceCategory(category.id()));
        memorySource.addPlaceCategory(category);
        Assert.assertEquals(memorySource.getPlaceCategory(category.id()), category);
    }

    @Test
    public void dbSource_addsCategory() {
        PlaceCategory category = generateRandomCategory();
        Assert.assertNull(dbSource.getPlaceCategory(category.id()));
        dbSource.addPlaceCategory(category);
        Assert.assertEquals(dbSource.getPlaceCategory(category.id()), category);
    }

    @Test
    public void networkSource_loadsCategories() {
        Assert.assertTrue(repository.reloadFromNetwork());
        Assert.assertNotNull(repository.getPlaceCategory(TEST_CATEGORY_ID));
    }

    @Test
    public void repository_cachesCategory() {
        Assert.assertTrue(repository.reloadFromNetwork());
        Assert.assertNotNull(repository.getPlaceCategory(TEST_CATEGORY_ID));
        Assert.assertNotNull(dbSource.getPlaceCategory(TEST_CATEGORY_ID));
        Assert.assertNotNull(memorySource.getPlaceCategory(TEST_CATEGORY_ID));
    }

    private PlaceCategory generateRandomCategory() {
        Random random = new Random(System.currentTimeMillis());

        return PlaceCategory.builder()
                .id(random.nextLong())
                .name(String.valueOf(random.nextLong()))
                .build();
    }
}