import React from "react";
import { Link, useLocation } from "react-router-dom";
import {
  Compass,
  Home,
  LogIn,
  LogOut,
  PlusCircle,
  Search,
  Shield,
  User,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useAuthContext } from "@/contexts/AuthContext";
import { HeartsIndicator } from "./HeartsIndicator";
import { useIsDesktop } from "@/hooks/use-desktop";

const navItems = [
  { label: "Learn", href: "/lessons", icon: Home },
  { label: "Feed", href: "/", icon: Compass },
  { label: "Explore", href: "/explore", icon: Search },
  { label: "Profile", href: "/profile", icon: User },
];

export function DesktopNav() {
  const location = useLocation();
  const { isAuthenticated, isAdmin, logout } = useAuthContext();
  const isDesktop = useIsDesktop();

  return (
    <header className="hidden lg:flex fixed top-0 left-0 right-0 z-50 border-b border-mainAlt bg-mainDark/95 backdrop-blur">
      <div className="grid h-16 w-full grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-4 px-4 xl:gap-6 xl:px-6">
        <Link to="/" className="flex items-center gap-3 min-w-fit">
          <img
            src="/icon-images/LEADERBOARD_ICON.png"
            alt=""
            className="h-9 w-9 rounded-md object-cover"
          />
          <span className="text-mainAccent dark:text-white text-xl">Rotiprata</span>
        </Link>

        <div className="min-w-0 overflow-x-auto scrollbar-hide">
          <nav className="mx-auto flex w-max min-w-0 items-center gap-1 px-2 xl:gap-2" aria-label="Primary">
            {navItems.map((item) => {
              const isActive =
                location.pathname === item.href ||
                (item.href !== "/" && location.pathname.startsWith(item.href));
              const Icon = item.icon;

              return (
                <Link
                  key={item.href}
                  to={item.href}
                  className={cn(
                    "shrink-0 flex items-center gap-2 rounded-xl border px-3 py-2 transition-colors",
                    isActive
                      ? "border-mainAlt bg-main text-mainAccent dark:text-white"
                      : "border-transparent text-mainAccent/80 hover:text-mainAccent hover:border-mainAlt hover:bg-main/60 dark:text-white dark:hover:text-white"
                  )}
                >
                  <Icon className="h-4 w-4" />
                  <span className="text-sm">{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>

        <div className="flex items-center gap-2 min-w-fit justify-self-end">
          {isAuthenticated && isDesktop && <HeartsIndicator className="px-2.5 xl:px-3" />}

          <Button asChild className="duo-button-primary h-10 px-3 xl:px-4">
            <Link to="/create" aria-label="Create">
              <PlusCircle className="h-4 w-4 xl:mr-2" />
              <span className="hidden xl:inline">Create</span>
            </Link>
          </Button>

          {isAdmin() && (
            <Button
              asChild
              variant="outline"
              className="h-10 border-mainAlt bg-main px-3 text-mainAccent hover:bg-mainAlt dark:text-white xl:px-4"
            >
              <Link to="/admin" aria-label="Admin">
                <Shield className="h-4 w-4 xl:mr-2" />
                <span className="hidden xl:inline">Admin</span>
              </Link>
            </Button>
          )}

          {isAuthenticated ? (
            <Button
              type="button"
              variant="ghost"
              aria-label="Log Out"
              className="h-10 px-3 text-mainAccent hover:bg-mainAlt hover:text-mainAccent dark:text-white dark:hover:text-white xl:px-4"
              onClick={() => {
                void logout();
              }}
            >
              <LogOut className="h-4 w-4 xl:mr-2" />
              <span className="hidden xl:inline">Log Out</span>
            </Button>
          ) : (
            <Button asChild className="duo-button-primary h-10">
              <Link to="/login">
                <LogIn className="h-4 w-4 mr-2" />
                Sign In
              </Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
