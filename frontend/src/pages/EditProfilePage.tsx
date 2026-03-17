import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

import { MainLayout } from '@/components/layout/MainLayout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import { useAuthContext } from '@/contexts/AuthContext';
import { checkDisplayNameAvailability, fetchProfile, updateProfile } from '@/lib/api';
import {
  isDisplayNameFormatValid,
  DISPLAY_NAME_POLICY_MESSAGE,
  normalizeDisplayName,
} from '@/lib/displayNamePolicy';

const EditProfilePage = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuthContext();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const [displayName, setDisplayName] = useState('');
  const [originalDisplayName, setOriginalDisplayName] = useState('');
  const [isGenAlpha, setIsGenAlpha] = useState(false);
  const [displayNameStatus, setDisplayNameStatus] = useState<'idle' | 'checking' | 'available' | 'taken' | 'invalid' | 'error'>('idle');
  const availabilityRequestId = useRef(0);

  useEffect(() => {
    if (!isAuthenticated) return;
    fetchProfile()
      .then((profile) => {
        const currentDisplayName = profile.display_name ?? '';
        setDisplayName(currentDisplayName);
        setOriginalDisplayName(currentDisplayName);
        setIsGenAlpha(Boolean(profile.is_gen_alpha));
      })
      .catch((err) => {
        console.error('Failed to load profile', err);
        setError('Unable to load your profile. Please try again.');
      })
      .finally(() => setLoading(false));
  }, [isAuthenticated]);

  const normalizedDisplayName = useMemo(() => normalizeDisplayName(displayName), [displayName]);
  const normalizedOriginalDisplayName = useMemo(() => normalizeDisplayName(originalDisplayName), [originalDisplayName]);

  useEffect(() => {
    if (!displayName.trim()) {
      availabilityRequestId.current += 1;
      setDisplayNameStatus('idle');
      return;
    }
    if (!isDisplayNameFormatValid(displayName)) {
      availabilityRequestId.current += 1;
      setDisplayNameStatus('invalid');
      return;
    }
    if (normalizedDisplayName === normalizedOriginalDisplayName) {
      availabilityRequestId.current += 1;
      setDisplayNameStatus('available');
      return;
    }

    setDisplayNameStatus('checking');
    const requestId = ++availabilityRequestId.current;
    const candidate = displayName;
    const timeout = window.setTimeout(async () => {
      try {
        const result = await checkDisplayNameAvailability(candidate);
        if (availabilityRequestId.current !== requestId) {
          return;
        }
        setDisplayNameStatus(result.available ? 'available' : 'taken');
      } catch (err) {
        if (availabilityRequestId.current !== requestId) {
          return;
        }
        console.error('Display name check failed', err);
        setDisplayNameStatus('error');
      }
    }, 400);

    return () => window.clearTimeout(timeout);
  }, [displayName, normalizedDisplayName, normalizedOriginalDisplayName]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setMessage('');

    if (!isDisplayNameFormatValid(displayName)) {
      setError(DISPLAY_NAME_POLICY_MESSAGE);
      return;
    }
    if (displayNameStatus === 'checking') {
      setError('Checking display name availability. Please wait.');
      return;
    }
    if (displayNameStatus === 'taken') {
      setError('Display name already in use.');
      return;
    }
    if (displayNameStatus === 'error') {
      setError('Unable to verify display name availability. Please try again.');
      return;
    }

    const normalizedToSave = normalizeDisplayName(displayName);
    setSaving(true);
    try {
      await updateProfile({
        display_name: normalizedToSave,
        is_gen_alpha: isGenAlpha,
      });
      setDisplayName(normalizedToSave);
      setOriginalDisplayName(normalizedToSave);
      setMessage('Profile updated successfully.');
      window.setTimeout(() => navigate('/profile'), 500);
    } catch (err) {
      console.error('Failed to update profile', err);
      setError('Unable to update your profile right now. Please try again.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <MainLayout>
        <div className="container max-w-md mx-auto px-4 py-16 text-center text-muted-foreground">
          Loading profile...
        </div>
      </MainLayout>
    );
  }

  const isSubmitDisabled =
    saving
    || displayNameStatus === 'checking'
    || displayNameStatus === 'taken'
    || displayNameStatus === 'invalid';

  return (
    <MainLayout>
      <div className="container max-w-md mx-auto px-4 py-6 md:py-8">
        <Link to="/profile" className="inline-flex items-center text-muted-foreground hover:text-foreground mb-4">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Profile
        </Link>

        <Card>
          <CardHeader>
            <CardTitle>Edit Profile</CardTitle>
            <CardDescription>Update the profile fields you set during sign up.</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {message && <div className="p-3 rounded-lg bg-success/10 text-success text-sm">{message}</div>}
              {error && <div className="p-3 rounded-lg bg-destructive/10 text-destructive text-sm">{error}</div>}

              <div className="space-y-2">
                <Label htmlFor="displayName">Display name</Label>
                <Input
                  id="displayName"
                  type="text"
                  value={displayName}
                  onChange={(e) => setDisplayName(e.target.value)}
                  required
                />
                {displayName && (
                  <p className="text-xs text-muted-foreground">
                    {displayNameStatus === 'checking' && 'Checking availability...'}
                    {displayNameStatus === 'available' && 'Display name is available.'}
                    {displayNameStatus === 'taken' && 'Display name is already taken.'}
                    {displayNameStatus === 'invalid' && DISPLAY_NAME_POLICY_MESSAGE}
                    {displayNameStatus === 'error' && 'Unable to check display name right now.'}
                  </p>
                )}
              </div>

              <div className="flex items-center space-x-2">
                <Checkbox
                  id="isGenAlpha"
                  checked={isGenAlpha}
                  onCheckedChange={(checked) => setIsGenAlpha(!!checked)}
                />
                <Label htmlFor="isGenAlpha" className="text-sm">
                  I was born in 2010 or later (Gen Alpha)
                </Label>
              </div>

              <Button type="submit" className="w-full" disabled={isSubmitDisabled}>
                {saving ? 'Saving...' : 'Save Changes'}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </MainLayout>
  );
};

export default EditProfilePage;
