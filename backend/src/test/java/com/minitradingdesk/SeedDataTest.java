package com.minitradingdesk;

import static org.assertj.core.api.Assertions.assertThat;

import com.minitradingdesk.account.AccountRepository;
import com.minitradingdesk.auth.AppUser;
import com.minitradingdesk.auth.AppUserRepository;
import com.minitradingdesk.auth.Role;
import com.minitradingdesk.instrument.InstrumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SeedDataTest {
    @Autowired
    private AppUserRepository users;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private InstrumentRepository instruments;

    @Test
    void seedsTwelveInstrumentsWithLotSize100() {
        assertThat(instruments.findAll())
                .hasSize(12)
                .allSatisfy(i -> assertThat(i.getLotSize()).isEqualTo(100));
        assertThat(instruments.findBySymbol("EURUSD")).isPresent();
    }

    @Test
    void seedsTwoTradersAndOneAdminWithBcryptPasswords() {
        AppUser trader = users.findByUsername("trader1").orElseThrow();
        AppUser admin = users.findByUsername("admin").orElseThrow();

        assertThat(trader.getRole()).isEqualTo(Role.TRADER);
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(new BCryptPasswordEncoder().matches("password", trader.getPasswordHash())).isTrue();
    }

    @Test
    void eachTraderHasAnAccountWithStartingCash() {
        assertThat(accounts.findByUsername("trader1").orElseThrow().getCashBalance())
                .isEqualByComparingTo("1000000");
        assertThat(accounts.findByUsername("trader2")).isPresent();
        assertThat(accounts.findByUsername("admin")).isEmpty();
    }
}