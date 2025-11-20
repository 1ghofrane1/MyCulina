MyCulina: “Cook it. Love it. Share it.”

Réalisé par:
Ghofrane Bouallegue - FIE4

Nom: 
MyCulina vient du mot latin “culina”, qui signifie “cuisine”. 

Objectif:
MyCulina est une application mobile Android conçue pour aider les utilisateurs à découvrir, organiser et gérer des recettes de cuisine. Elle permet de :
  * Parcourir une large variété de recettes,
  * Consulter des instructions détaillées,
  * Enregistrer des recettes favorites,
  * Ajouter ses propres créations culinaires.

L'application vise à :
  * Simplifier la planification des repas,
  * Stimuler la créativité culinaire,
  * Et améliorer l'expérience utilisateur, même sans connexion Internet.

Fonctionnalités principales : 
  1. Authentification
  Les utilisateurs peuvent se connecter via Google Authentication (Firebase) afin d'accéder à leurs recettes favorites et personnelles.
  2. Écran d'accueil (Liste des recettes)
  Affiche les recettes provenant de TheMealDB API, avec mise en cache locale via Room pour un accès rapide et hors ligne.
  3. Écran Détails de la recette
  Contient les ingrédients, étapes de préparation et images.
  Les utilisateurs peuvent marquer une recette comme favorite.
  4. Écran Favoris
  Montre toutes les recettes sauvegardées par l'utilisateur, stockées localement dans Room et disponibles même sans connexion.
  5. Écran Ajouter / Gérer ses propres recettes
  Permet d'ajouter, modifier ou supprimer ses recettes personnelles. Les recettes créées sont enregistrées localement dans Room.

Exigences techniques
  * Authentification
    Firebase Authentication est utilisé uniquement pour l'authentification Google.
  * Connexion réseau
    Communication avec TheMealDB API pour récupérer les recettes en ligne.
  * Stockage local
    Room Database assure le stockage des recettes favorites et des recettes créées par l'utilisateur pour un usage hors ligne.
  * Architecture utilisée
    MVVM (Model - View - ViewModel)

Architecture du système :
View <--> ViewModel <--> Repository <--> Room <--> TheMealDB API
                     |        
                     | ---> Firebase Authentication

* View (Vue) :
  Interface utilisateur (écrans de liste, détails, favoris, ajout). Affiche les données et capture les actions.
* ViewModel :
  Sert d'intermédiaire entre la Vue et le Repository. Fournit les données à afficher et gère la logique d'affichage selon l'état réseau.
* Repository :
  Gère la récupération et la mise à jour des données depuisTheMealDB API et Room.
* Firebase Authentication :
  Gère l'authentification Google / Email&Password des utilisateurs.
* Model :
  Contient les structures de données (recette, utilisateur) et la logique métier.
