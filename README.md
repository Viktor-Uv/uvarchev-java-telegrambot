<!-- PROJECT SHIELDS -->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="images/telegram-spacenews-logo.png">
    <img src="images/telegram-spacenews-logo.png" alt="Logo" width="640" height="360">
  </a>

<h2 align="center">Telegram Spacenews Subscribe Bot</h2>

  <p align="center">
    Bot allows users to subscribe to various space-related news providers using Spaceflight News API and sends hourly updates (when available).
    <br />
    News providers available for subscription: Arstechnica, European Spaceflight, NASA, NASASpaceflight, SpaceNews, SpacePolicyOnline.com, and Space Scout.
    <br />
    <a href="https://github.com/Viktor-Uv/uvarchev-java-telegrambot/"><strong>Explore the project »</strong></a>
    <br />
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#technologies">Technologies used</a></li>
    <li><a href="#architecture">Architecture</a></li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#installation">Installation</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>




<!-- ABOUT THE PROJECT -->
## About The Project

##### Bot was created to provide space enthusiasts with the hourly dose of space news from their favourite providers directly in the telegram messenger.

![Product Name Screen Shot][product-screenshot]

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Technologies
* Language - Java 17
* Framework - Spring Boot 3
* Database - MySQL
* Working with database - Hibernate, JPA
* Build system - Maven
* Testing - Mockito, JUnit
* Scheduling - Cron
* Code documentation - JavaDoc
* Logging - SLF4J
* Version control - GitHub


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- Architecture -->
## Architecture

-	**Presentation Layer:** Telegram Bots API acts as the Controller. Users can find the bot by its telegram name @uvarchev_telebot; they can initialise the chat via “/start” command. Users will then get a choice of available providers, which users can subscribe to.
-	**Business Logic Layer:** processes user inputs, sends requests to the chosen services, updates database with user preferences, sets subscriptions, returns user data received from the external service providers.
-	**Data Access Layer:** creates, reads, updates, and deletes user credentials and their settings.
-   **Database:** stores user credentials and their settings, as well as the data received from the external services.


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Usage

Find this bot by its username in the telegram messenger: @uvarchev_telebot.
#### Commands available:
* /start - Register to start using the bot
* /stop - Stop receiving updates
* /subscribe - Usage: /subscribe [provider, or ALL]
* /unsubscribe - Usage: /unsubscribe [provider, or ALL]
* /subscriptions - List of current active subscriptions and available ones


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- Installation -->
## Installation

Fork your own copy of the repository. Create “env.properties” file in the main directory and fill up your credentials:
-	DB_DATABASE=
-	DB_USERNAME=
-	DB_PASSWORD=
-	TELEBOT_TOKEN=
-	TELEBOT_NAME=


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the MIT Licence. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

Telegtam - https://t.me/Viktor_Uvr

Project Link: https://github.com/Viktor-Uv/uvarchev-java-telegrambot

<p align="right">(<a href="#readme-top">back to top</a>)</p>







<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/Viktor-Uv/uvarchev-java-telegrambot.svg?style=for-the-badge
[contributors-url]: https://github.com/Viktor-Uv/uvarchev-java-telegrambot/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/Viktor-Uv/uvarchev-java-telegrambot.svg?style=for-the-badge
[forks-url]: https://github.com/Viktor-Uv/uvarchev-java-telegrambot/network/members
[stars-shield]: https://img.shields.io/github/stars/Viktor-Uv/uvarchev-java-telegrambot.svg?style=for-the-badge
[stars-url]: https://github.com/Viktor-Uv/uvarchev-java-telegrambot/stargazers
[issues-shield]: https://img.shields.io/github/issues/Viktor-Uv/uvarchev-java-telegrambot.svg?style=for-the-badge
[license-shield]: https://img.shields.io/github/license/Viktor-Uv/uvarchev-java-telegrambot.svg?style=for-the-badge
[license-url]: https://github.com/Viktor-Uv/uvarchev-java-telegrambot/blob/master/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/viktor-uvarchev/
[product-screenshot]: images/screenshot.png
[Next.js]: https://img.shields.io/badge/next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white
[Next-url]: https://nextjs.org/
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Vue.js]: https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D
[Vue-url]: https://vuejs.org/
[Angular.io]: https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white
[Angular-url]: https://angular.io/
[Svelte.dev]: https://img.shields.io/badge/Svelte-4A4A55?style=for-the-badge&logo=svelte&logoColor=FF3E00
[Svelte-url]: https://svelte.dev/
[Laravel.com]: https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white
[Laravel-url]: https://laravel.com
[Bootstrap.com]: https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white
[Bootstrap-url]: https://getbootstrap.com
[JQuery.com]: https://img.shields.io/badge/jQuery-0769AD?style=for-the-badge&logo=jquery&logoColor=white
[JQuery-url]: https://jquery.com 
