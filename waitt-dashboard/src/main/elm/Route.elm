module Route exposing (Route(..), fromUrl, href, replaceUrl)

import Browser.Navigation as Nav
import Html exposing (Attribute)
import Html.Attributes as Attr
import Url exposing (Url)
import Url.Parser as Parser exposing ((</>), Parser, oneOf, s, string)

-- ROUTING

type Route
    = Home
    | Root
    | ThreadDump
    | HeapDump
    | Environments
    | Server

parser : Parser (Route -> a) a
parser =
    oneOf
        [ Parser.map Home Parser.top
        , Parser.map ThreadDump (s "thread")
        , Parser.map HeapDump (s "heap")
        , Parser.map Environments (s "environments")
        , Parser.map Server (s "server")
        ]

-- PUBLIC HELPERS

href : Route -> Attribute msg
href targetRoute =
    Attr.href (routeToString targetRoute)


replaceUrl : Nav.Key -> Route -> Cmd msg
replaceUrl key route =
    Nav.replaceUrl key (routeToString route)


fromUrl : Url -> Maybe Route
fromUrl url =
    -- The RealWorld spec treats the fragment like a path.
    -- This makes it *literally* the path, so we can proceed
    -- with parsing as if it had been a normal path all along.
    { url | path = Maybe.withDefault "" url.fragment, fragment = Nothing }
        |> Parser.parse parser


-- INTERNAL


routeToString : Route -> String
routeToString page =
    let
        pieces =
            case page of
                Home ->
                    []

                Root ->
                    []

                ThreadDump ->
                    [ "thread" ]

                HeapDump ->
                    [ "heap" ]

                Environments ->
                    [ "environments" ]

                Server ->
                    [ "server" ]

    in
        "#/" ++ String.join "/" pieces
