package com.example.mytasbeehapp.presentation.screens.PrivacyPolicy


import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.mytasbeehapp.presentation.andriodview.LegalDocumentScreen
import com.example.mytasbeehapp.presentation.andriodview.LegalSection

private val privacySections = listOf(
    LegalSection(
        body = """This privacy policy applies to the My Tasbeeh App app for mobile devices, together with any related services operated by Umair Khan (collectively, the "Application"). Umair Khan is hereby referred to as the "Service Provider"."""
    ),
    LegalSection(
        heading = "Information Collection and Use",
        body = "The Application collects information when you download and use it. This information may include information such as",
        bullets = listOf(
            "Your device's Internet Protocol address",
            "The pages of the Application that you visit, the time and date of your visit, the time spent on those pages",
            "The time spent on the Application",
            "Your mobile operating system you use"
        )
    ),
    LegalSection(
        heading = "Cookies and Tracking Technologies",
        body = "The Application or its third-party SDKs may use cookies, SDKs, pixels, and similar technologies to support functionality, analytics, or service delivery. Where required by applicable law, the Service Provider will obtain consent before using non-essential tracking technologies."
    ),
    LegalSection(
        heading = "Your Rights",
        body = "You may request access to, correction of, or deletion of your personal data held by the Service Provider. To exercise these rights, or to withdraw consent where processing is based on consent, contact the Service Provider at umairkhan9920933@gmail.com."
    ),
    LegalSection(
        heading = "Your California Privacy Rights (CCPA/CPRA)",
        body = "If you are a California resident, you have the right to know what personal information is collected, the right to delete personal information, the right to opt out of the sale or sharing of personal information, and the right to non-discrimination for exercising these rights. To exercise your CCPA/CPRA rights, contact the Service Provider at umairkhan9920933@gmail.com."
    ),
    LegalSection(
        body = "The Service Provider may use the information you provide to send important information, required notices, and, where permitted by law, marketing communications."
    ),
    LegalSection(
        body = "For a better experience while using the Application, the Service Provider may require you to provide certain personally identifiable information. The information the Service Provider requests will be retained and used as described in this privacy policy."
    ),
    LegalSection(
        heading = "Third Party Access",
        body = "Only aggregated, anonymized data is periodically transmitted to external services to aid the Service Provider in improving the Application and their service. The Service Provider may share your information with third parties in the ways that are described in this privacy statement."
    ),
    LegalSection(
        heading = "International Data Transfers",
        body = "The Service Provider or its third-party service providers may transfer personal data to countries outside your country of residence, including outside the European Economic Area (EEA). Where applicable law requires safeguards for international transfers, the Service Provider will use appropriate mechanisms.",
        bullets = listOf(
            "Standard Contractual Clauses (SCCs) approved by the European Commission",
            "Adequacy decisions or other legally recognized transfer mechanisms",
            "Your consent, where required and legally permitted"
        )
    ),
    LegalSection(
        body = "Data protection laws in other countries may differ from those in your jurisdiction. Where required by law, the Service Provider will apply appropriate safeguards and obtain any consent required for the transfer."
    ),
    LegalSection(
        heading = "Third-Party Services",
        body = "Please note that the Application utilizes third-party services that have their own Privacy Policy about handling data. Below are the third-party service providers used by the Application:",
        bullets = listOf(
            "Google Play Services",
            "AdMob",
            "Google Analytics for Firebase"
        )
    ),
    LegalSection(
        body = "The Service Provider may disclose User Provided and Automatically Collected Information:",
        bullets = listOf(
            "As required by law, such as to comply with a subpoena, or similar legal process",
            "When they believe in good faith that disclosure is necessary to protect their rights, protect your safety or the safety of others, investigate fraud, or respond to a government request",
            "With their trusted service providers who work on their behalf, do not have an independent use of the information the Service Provider discloses to them, and have agreed to adhere to the rules set forth in this privacy statement"
        )
    ),
    LegalSection(
        heading = "Opt-Out Rights",
        body = "You can stop further collection of information from your mobile device by uninstalling the Application. Uninstalling will stop the Application from collecting data from your device, but it does not automatically delete information that has already been transmitted to the Service Provider or to third parties."
    ),
    LegalSection(
        body = "To request deletion of your personal data, to withdraw consent, or to exercise any of your rights, contact the Service Provider at umairkhan9920933@gmail.com."
    ),
    LegalSection(
        heading = "Data Retention Policy",
        body = "The Service Provider retains personal data based on its necessity for the stated purposes:",
        bullets = listOf(
            "User Provided Data: retained for the duration of your use of the Application plus 12 months thereafter, unless longer retention is required by law",
            "Automatically Collected Data: retained for up to 24 months from collection, unless longer retention is required for legal compliance",
            "Aggregated and Anonymized Data: retained indefinitely as it no longer identifies you",
            "Data required for legal compliance: retained as long as required by applicable law"
        )
    ),
    LegalSection(
        body = "You may request deletion of your personal data, subject to any legal obligation to retain it. If you want the Service Provider to delete User Provided Data submitted through the Application, please contact them at umairkhan9920933@gmail.com. Please note that some User Provided Data may be required for the Application to function properly."
    ),
    LegalSection(
        heading = "Children",
        body = "The Application is not intended for children under 16 years of age, or such higher age as required by applicable law. The Service Provider does not knowingly solicit data from children or market the Application to them."
    ),
    LegalSection(
        body = "Where parental or guardian consent is required under applicable law, the Application is not intended for use without that consent. The Service Provider does not knowingly collect personally identifiable information from children under 16 years of age in violation of applicable law. In the event the Service Provider discovers that a child has provided personal information, the Service Provider will immediately delete this from their servers. If you are a parent or guardian and you are aware that your child has provided the Service Provider with personal information, please contact the Service Provider (umairkhan9920933@gmail.com) so that they will be able to take the necessary actions."
    ),
    LegalSection(
        heading = "Security",
        body = "The Service Provider is concerned about safeguarding the confidentiality of your information. The Service Provider provides physical, electronic, and procedural safeguards to protect information the Service Provider processes and maintains."
    ),
    LegalSection(
        heading = "Data Breach Notification",
        body = "If a data breach occurs that affects your personal data, the Service Provider will notify you in accordance with applicable legal requirements, including, where required, providing information about the nature of the breach and the steps being taken to address it."
    ),
    LegalSection(
        heading = "Changes",
        body = "The Service Provider may update this Privacy Policy from time to time. The Service Provider will notify you of material changes by posting the updated Privacy Policy with an effective date. Where required by law, the Service Provider will seek your consent to material changes before they take effect."
    ),
    LegalSection(
        body = "Previous versions of this Privacy Policy will be maintained and made available upon request by contacting the Service Provider at umairkhan9920933@gmail.com."
    ),
    LegalSection(
        body = "This privacy policy is effective as of 2026-10-16."
    ),
    LegalSection(
        heading = "Your Consent",
        body = "Where processing is based on consent, you provide that consent by affirmatively opting in to the relevant feature or action. You may withdraw consent at any time without affecting processing carried out before withdrawal. Processing based on other lawful bases is carried out as described above."
    ),
    LegalSection(
        heading = "Contact Us",
        body = "If you have any questions regarding privacy while using the Application, or have questions about the practices, please contact the Service Provider via email at umairkhan9920933@gmail.com."
    )
)

@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    LegalDocumentScreen(
        title = "Privacy Policy",
        effectiveDate = "October 16, 2026",
        sections = privacySections,
        contactEmail = "umairkhan9920933@gmail.com",
        onBack = { navController.popBackStack() }
    )
}