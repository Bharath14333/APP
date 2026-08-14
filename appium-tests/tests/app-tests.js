const wdio = require('webdriverio');
const assert = require('assert');

// Appium connection configuration capabilities
const opts = {
    path: '/wd/hub',
    port: 4723,
    capabilities: {
        platformName: "Android",
        platformVersion: "14.0",
        deviceName: "Pixel_8_Emulator",
        app: "c:\\Users\\bhara\\Documents\\CisisSenseApp\\app\\build\\outputs\\apk\\debug\\app-debug.apk",
        appPackage: "com.emergency.crisissense",
        appActivity: ".activity.SplashActivity",
        automationName: "UiAutomator2",
        noReset: true
    }
};

describe('CrisisSense Android Native Appium E2E Suite', function () {
    let client;
    this.timeout(40000);

    before(async function () {
        client = await wdio.remote(opts);
    });

    after(async function () {
        if (client) {
            await client.deleteSession();
        }
    });

    it('TC-01: Verify Splash Screen Transition to Welcome Page', async function () {
        // Wait for Splash screen to transition to WelcomeActivity
        await client.pause(3000);
        const welcomeTitle = await client.$('id=txt_welcome_title');
        assert.ok(await welcomeTitle.isDisplayed(), 'Welcome screen title should be visible after splash transition');
    });

    it('TC-02: Verify Auth Input Rules & Validation', async function () {
        const btnGoLogin = await client.$('id=btn_go_login');
        await btnGoLogin.click();

        await client.pause(1000);
        const editEmail = await client.$('id=edit_login_email');
        const editPassword = await client.$('id=edit_login_password');
        const btnSubmit = await client.$('id=btn_login_submit');

        await editEmail.setValue('invalid-email-format');
        await editPassword.setValue('12');
        await btnSubmit.click();

        const toastMsg = await client.$('xpath=//android.widget.Toast');
        assert.ok(await toastMsg.isDisplayed(), 'Validation toast warning should display for invalid credentials');
    });

    it('TC-03: Verify Offline Mock Bypass Login Session', async function () {
        const editEmail = await client.$('id=edit_login_email');
        const editPassword = await client.$('id=edit_login_password');
        const btnSubmit = await client.$('id=btn_login_submit');

        await editEmail.clearValue();
        await editEmail.setValue('citizen@crisissense.org');
        await editPassword.clearValue();
        await editPassword.setValue('demo1234');
        await btnSubmit.click();

        // Redirects to MainActivity / Dashboard Fragment
        await client.pause(4000);
        const txtDashboardWelcome = await client.$('id=txt_welcome');
        assert.ok(await txtDashboardWelcome.isDisplayed(), 'Bypass login must redirect user to main citizen dashboard fragment');
    });

    it('TC-04: Verify SOS Button Hold Activation Gesture', async function () {
        const cardReport = await client.$('id=card_report_emergency');
        await cardReport.click();

        await client.pause(1000);
        const btnSosTrigger = await client.$('id=btn_sos_trigger');
        assert.ok(await btnSosTrigger.isDisplayed(), 'SOS trigger button should be visible');

        // Simulate 3-second long press gesture to activate SOS
        await client.touchAction([
            { action: 'longPress', element: btnSosTrigger, duration: 3200 },
            { action: 'release' }
        ]);

        await client.pause(1500);
        const layoutTriggered = await client.$('id=layout_sos_triggered');
        assert.ok(await layoutTriggered.isDisplayed(), 'SOS broadcast details card should display after 3s hold trigger');
    });

    it('TC-05: Verify Emergency Contacts CRUD Panel', async function () {
        const btnBack = await client.$('id=btn_back');
        await btnBack.click(); // return to dashboard

        await client.pause(1000);
        const cardContacts = await client.$('id=card_contacts');
        await cardContacts.click();

        await client.pause(1000);
        const btnAdd = await client.$('id=btn_add_contact');
        await btnAdd.click();

        // Populate details in dialog
        const editName = await client.$('xpath=//android.widget.EditText[@hint="Contact Name"]');
        const editPhone = await client.$('xpath=//android.widget.EditText[@hint="Phone Number"]');
        
        await editName.setValue('Jane Doe');
        await editPhone.setValue('9876543210');
        
        const btnSave = await client.$('xpath=//android.widget.Button[@text="Save"]');
        await btnSave.click();

        await client.pause(1000);
        const contactNameCell = await client.$('xpath=//android.widget.TextView[@text="Jane Doe"]');
        assert.ok(await contactNameCell.isDisplayed(), 'Newly added personal contact should be displayed in the list row');
    });
});
