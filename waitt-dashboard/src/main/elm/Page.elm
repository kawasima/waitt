module Page exposing (Page(..), view, viewErrors)

import Browser exposing (Document)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Route exposing (Route)

type Page
    = Other
    | Home

view : Page -> { title : String, content : Html msg } -> Document msg
view page { title, content } =
    { title = title ++ " - Waitt"
    , body = [ div [ class "c-app" ]
                   [ viewSidebar page
                   , div [ class "c-wrapper"]
                         [ div [ class "c-body" ]
                           [ div [ class "container" ]
                             [ viewMain page content ]
                           ]
                         ]
                   ]
             ]
    }

viewSidebar : Page -> Html msg
viewSidebar page =
    div [ class "c-sidebar c-sidebar-show c-sidebar-fixed" ]
        [ div [ class "c-sidebar-brand" ]
              [ text "WAITT dashboard" ]
        , nav [ class "c-sidebar-nav" ]
          [ ul [ class "c-nav wd-sidebar-nav" ]
            [ li [ class "c-nav-item" ]
              [ a [ class "c-nav-link", Route.href Route.Home ]
               [ text "Application" ]
              ]
            , li [ class "c-nav-item" ]
              [ a [ class "c-nav-link", Route.href Route.ThreadDump ]
               [ text "Thread dump" ]
              ]
            , li [ class "c-nav-item" ]
              [ a [ class "c-nav-link", Route.href Route.HeapDump ]
               [ text "Heap dump" ]
              ]
            , li [ class "c-nav-item" ]
              [ a [ class "c-nav-link", Route.href Route.Environments ]
               [ text "Environments" ]
              ]
            , li [ class "c-nav-item" ]
              [ a [ class "c-nav-link", Route.href Route.Server ]
               [ text "Server" ]
              ]
            ]
          ]
        ]

viewMain : Page -> Html msg -> Html msg
viewMain page content =
    div [ class "c-main" ]
        [ content ]

navbarLink : Page -> Route -> List (Html msg) -> Html msg
navbarLink  page route linkContent =
    a [ classList [ ( "menu-item", True), ( "selected", isActive page route ) ]
      , Route.href route ]
        linkContent

isActive : Page -> Route -> Bool
isActive page route =
    case ( page, route ) of
        ( Home, Route.Home ) ->
            True
        _ ->
            False

viewErrors : msg -> List String -> Html msg
viewErrors dismissErrors errors =
    if List.isEmpty errors then
        Html.text ""

    else
        div
            [ class "error-messages"
            , style "position" "fixed"
            ]
        <|
            List.map (\error -> p [] [ text error ]) errors
                ++ [ button [ onClick dismissErrors ] [ text "Ok" ] ]
