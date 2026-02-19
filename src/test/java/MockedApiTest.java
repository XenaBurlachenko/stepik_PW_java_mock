import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class MockedApiTest {
    static Playwright playwright;
    static Browser browser;
    private BrowserContext context;
    private Page page;
    private static TestApiService apiService;

    @BeforeAll
    static void setUpClass() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
            .setHeadless(true)
            .setChannel("chrome")); 
        
        apiService = new TestApiService();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @Test
    void testUserProfileWithMockedApi() {
        // Получаем тестовые данные
        String userData = apiService.fetchUserData();
        
        // Загружаем страницу
        page.navigate("https://the-internet.herokuapp.com/dynamic_content");
        
        // Внедряем данные на уже загруженную страницу
        page.evaluate("(data) => { window.userData = JSON.parse(data); }", userData);
        
        // Проверяем, что данные доступны в окне браузера
        Object userName = page.evaluate("() => window.userData.name");
        Object userEmail = page.evaluate("() => window.userData.email");
        
        // Проверяем результат
        assertNotNull(userName);
        assertNotNull(userEmail);
        assertEquals("Test User", userName);
        assertEquals("test@example.com", userEmail);
        
        // Получаем весь объект целиком
        Object result = page.evaluate("() => window.userData");
        assertNotNull(result);
        assertTrue(result.toString().contains("Test User"));
    }

    @Test
    // Альтернативная математика, через localStorage
    void testUserProfileWithAlternativeApproach() {
        
        String userData = apiService.fetchUserData();
        
        page.navigate("https://the-internet.herokuapp.com/dynamic_content");
        
        // Используем localStorage вместо window
        page.evaluate("(data) => { localStorage.setItem('userData', data); }", userData);
        
        // Проверяем через localStorage
        String storedData = (String) page.evaluate("() => localStorage.getItem('userData')");
        assertNotNull(storedData);
        assertTrue(storedData.contains("Test User"));
    }

    static class TestApiService {
        public String fetchUserData() {
            return "{\"name\": \"Test User\", \"email\": \"test@example.com\"}";
        }
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    @AfterAll
    static void tearDownClass() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }
}