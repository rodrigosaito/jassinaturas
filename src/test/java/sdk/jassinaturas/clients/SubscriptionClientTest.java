package sdk.jassinaturas.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import sdk.jassinaturas.Assinaturas;
import sdk.jassinaturas.clients.attributes.Address;
import sdk.jassinaturas.clients.attributes.Authentication;
import sdk.jassinaturas.clients.attributes.BillingInfo;
import sdk.jassinaturas.clients.attributes.Country;
import sdk.jassinaturas.clients.attributes.CreditCard;
import sdk.jassinaturas.clients.attributes.Customer;
import sdk.jassinaturas.clients.attributes.NextInvoiceDate;
import sdk.jassinaturas.clients.attributes.Plan;
import sdk.jassinaturas.clients.attributes.State;
import sdk.jassinaturas.clients.attributes.Subscription;
import sdk.jassinaturas.clients.attributes.SubscriptionStatus;
import sdk.jassinaturas.exceptions.ApiResponseErrorException;
import co.freeside.betamax.Betamax;
import co.freeside.betamax.MatchRule;
import co.freeside.betamax.Recorder;

public class SubscriptionClientTest {

    private final Assinaturas assinaturas = new Assinaturas(new Authentication("SGPA0K0R7O0IVLRPOVLJDKAWYBO1DZF3",
            "QUJESGM9JU175OGXRFRJIYM0SIFOMIFUYCBWH9WA"));

    @Rule
    public Recorder recorder = new Recorder();

    @Betamax(tape = "ACTIVATE_SUBSCRIPTION", match = { MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldActivateASubscription() {

        Subscription subscription = assinaturas.subscription().activate("assinatura06");

        // There isn't any response from Moip Assinaturas when subscription is
        // activated
        // So, I didn't do any assert

    }

    @Betamax(tape = "CANCEL_SUBSCRIPTION", match = { MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldCancelASubscription() {

        Subscription subscription = assinaturas.subscription().cancel("assinatura13");

        // There isn't any response from Moip Assinaturas when subscription is
        // canceled
        // So, I didn't do any assert

    }

    @Betamax(tape = "CREATE_SUBSCRIPTION",
            match = { MatchRule.body, MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldCreateANewSubscription() {
        Subscription toBeCreated = new Subscription();
        toBeCreated
                .withCode("subscription_with_new_customer_00001")
                .withAmount(100)
                .withPlan(new Plan().withCode("plan001"))
                .withCustomer(
                        new Customer()
                                .withCode("customer_created_with_subscription_0001")
                                .withBirthdate(new GregorianCalendar(1989, GregorianCalendar.OCTOBER, 13))
                                .withCpf("12312312312")
                                .withEmail("teste@teste.com")
                                .withFullname("Danillo Souza")
                                .withPhoneAreaCode("11")
                                .withPhoneNumber("912341234")
                                .withAddress(
                                        new Address().withCity("São Paulo").withComplement("Apto")
                                                .withCountry(Country.BRA).withDistrict("Centro").withNumber("1000")
                                                .withState(State.SP).withStreet("9 de Julho").withZipcode("10012345"))
                                .withBillingInfo(
                                        new BillingInfo().withCreditCard(new CreditCard().withExpirationMonth("10")
                                                .withExpirationYear("18").withHolderName("Danillo Souza")
                                                .withNumber("4111111111111111"))));

        Subscription created = assinaturas.subscription().create(toBeCreated);

        assertEquals("Assinatura criada com sucesso", created.getMessage());
        assertFalse(created.hasAlerts());
        assertFalse(created.hasErrors());

        assertEquals(created.getAmount(), 100);
        assertEquals(created.getPlan().getName(), "Plano de Teste Atualizado");
        assertEquals(created.getPlan().getCode(), "plan001");
        assertEquals(created.getStatus(), SubscriptionStatus.ACTIVE);
        assertEquals(created.getInvoice().getAmount(), 1100);
        assertEquals(created.getInvoice().getId(), 12872);
        assertEquals(created.getInvoice().getStatus().getDescription(), "Atrasada");
        assertEquals(created.getInvoice().getStatus().getCode(), 5);
        assertEquals(1, created.getNextInvoiceDate().get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MAY, created.getNextInvoiceDate().get(Calendar.MONTH));
        assertEquals(2014, created.getNextInvoiceDate().get(Calendar.YEAR));
        assertEquals(created.getCode(), "subscription_with_new_customer_00001");
        assertEquals(created.getCustomer().getEmail(), "teste@teste.com");
        assertEquals(created.getCustomer().getCode(), "customer_created_with_subscription_0001");
        assertEquals(created.getCustomer().getFullname(), "Danillo Souza");
    }

    @Betamax(tape = "CREATE_SUBSCRIPTION_WITHOUT_CUSTOMER", match = { MatchRule.body, MatchRule.method,
            MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldCreateANewSubscriptionWithoutANewCustomer() {
        Subscription toBeCreated = new Subscription();
        toBeCreated.withCode("subscription00001").withAmount(100)
                .withCustomer(new Customer().withCode("customer000000001")).withPlan(new Plan().withCode("plan001"));

        Subscription created = assinaturas.subscription().create(toBeCreated);

        assertEquals("Assinatura criada com sucesso", created.getMessage());
        assertFalse(created.hasAlerts());
        assertFalse(created.hasErrors());

        assertEquals(created.getAmount(), 100);
        assertEquals(created.getPlan().getName(), "Plano de Teste Atualizado");
        assertEquals(created.getPlan().getCode(), "plan001");
        assertEquals(created.getStatus(), SubscriptionStatus.ACTIVE);
        assertEquals(created.getInvoice().getAmount(), 1100);
        assertEquals(created.getInvoice().getId(), 12873);
        assertEquals(created.getInvoice().getStatus().getDescription(), "Atrasada");
        assertEquals(created.getInvoice().getStatus().getCode(), 5);
        assertEquals(1, created.getNextInvoiceDate().get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MAY, created.getNextInvoiceDate().get(Calendar.MONTH));
        assertEquals(2014, created.getNextInvoiceDate().get(Calendar.YEAR));
        assertEquals(created.getCode(), "subscription00001");
        assertEquals(created.getCustomer().getEmail(), "teste@teste.com");
        assertEquals(created.getCustomer().getCode(), "customer000000001");
        assertEquals(created.getCustomer().getFullname(), "Danillo Souza");
    }

    @Betamax(tape = "LIST_ALL_SUBSCRIPTIONS", match = { MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldListAllSubscriptions() {

        List<Subscription> subscriptions = assinaturas.subscription().list();
        assertEquals(60, subscriptions.size());
    }

    @Betamax(tape = "CREATE_SUBSCRIPTION_RETURNED_ERROR")
    @Test
    public void shouldReturnErrors() {
        Subscription toBeCreated = new Subscription();
        toBeCreated.withCode("subscription00001").withAmount(100)
                .withCustomer(new Customer().withCode("customer000000001")).withPlan(new Plan().withCode("plan001"));

        try {
            Subscription created = assinaturas.subscription().create(toBeCreated);
            fail("Should return ApiResponseError");
        } catch (ApiResponseErrorException e) {
            assertEquals("Erro na requisição", e.getApiResponseError().getMessage());
            assertEquals("Código da assinatura já utilizado. Escolha outro código", e.getApiResponseError().getErrors()
                    .get(0).getDescription());
            assertEquals("MA92", e.getApiResponseError().getErrors().get(0).getCode());
        }
    }

    @Betamax(tape = "GET_SINGLE_SUBSCRIPTION", match = { MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldShowASubscription() {

        Subscription subscription = assinaturas.subscription().show("subscription00001");

        assertEquals(0, subscription.getCreationDate().get(Calendar.MONTH));
        assertEquals(40, subscription.getCreationDate().get(Calendar.SECOND));
        assertEquals(Calendar.JANUARY, subscription.getCreationDate().get(Calendar.MONTH));
        assertEquals(2014, subscription.getCreationDate().get(Calendar.YEAR));
        assertEquals(23, subscription.getCreationDate().get(Calendar.HOUR_OF_DAY));
        assertEquals(21, subscription.getCreationDate().get(Calendar.DAY_OF_MONTH));

        assertEquals(100, subscription.getAmount());
        assertEquals(SubscriptionStatus.OVERDUE, subscription.getStatus());
        assertEquals("subscription00001", subscription.getCode());

        assertEquals("teste@teste.com", subscription.getCustomer().getEmail());
        assertEquals("customer000000001", subscription.getCustomer().getCode());
        assertEquals("Danillo Souza", subscription.getCustomer().getFullname());

        assertEquals("Plano de Teste Atualizado", subscription.getPlan().getName());
        assertEquals("plan001", subscription.getPlan().getCode());

        assertEquals(Calendar.OCTOBER, subscription.getExpirationDate().get(Calendar.MONTH));
        assertEquals(2016, subscription.getExpirationDate().get(Calendar.YEAR));
        assertEquals(17, subscription.getExpirationDate().get(Calendar.DAY_OF_MONTH));

        assertEquals(Calendar.MAY, subscription.getNextInvoiceDate().get(Calendar.MONTH));
        assertEquals(2014, subscription.getNextInvoiceDate().get(Calendar.YEAR));
        assertEquals(1, subscription.getNextInvoiceDate().get(Calendar.DAY_OF_MONTH));
    }

    @Betamax(tape = "SUSPEND_SUBSCRIPTION", match = { MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldSuspendASubscription() {

        Subscription subscription = assinaturas.subscription().suspend("assinatura07");

        // There isn't any response from Moip Assinaturas when subscription is
        // suspended
        // So, I didn't do any assert

    }

    @Betamax(tape = "UPDATE_SUBSCRIPTION", match = { MatchRule.method, MatchRule.headers, MatchRule.uri })
    @Test
    public void shouldUpdateASubscription() {

        Subscription toUpdate = new Subscription();
        toUpdate.withCode("assinatura46").withPlan(new Plan().withCode("plano01")).withAmount(990)
                .withNextInvoiceDate(new NextInvoiceDate().withDay(20).withMonth(Calendar.OCTOBER).withYear(2015));

        Subscription subscription = assinaturas.subscription().update(toUpdate);

        // There isn't any response from Moip Assinaturas when subscription is
        // updated
        // So, I didn't do any assert

    }
}
